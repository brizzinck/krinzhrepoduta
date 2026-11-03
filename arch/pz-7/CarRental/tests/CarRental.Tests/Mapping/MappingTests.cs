using System.Reflection;
using System.Text.Json;
using CarRental.Application.Fleet.Dto;
using CarRental.Application.Mapping;
using CarRental.Application.Rentals.Dto;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;
using CarRental.WebApi;
using CarRental.WebApi.Controllers;
using Microsoft.AspNetCore.Mvc;

namespace CarRental.Tests.Mapping;

public class MappingTests
{
    private static readonly AutoMapper.IMapper Mapper = TestMapper.Instance;

    [Fact]
    public void Configuration_of_all_profiles_is_valid() =>
        TestMapper.Configuration.AssertConfigurationIsValid();

    [Fact]
    public void Custom_converter_turns_money_into_amount_currency_and_text()
    {
        var dto = Mapper.Map<MoneyDto>(new Money(2400m));
        Assert.Equal(new MoneyDto(2400m, "UAH", "2400.00 UAH"), dto);
    }

    [Fact]
    public void Car_is_mapped_through_two_levels_to_the_api_response()
    {
        var car = TestData.Car();
        var dto = Mapper.Map<CarDto>(car);
        var response = Mapper.Map<CarResponse>(dto);
        Assert.Equal(new CarDto(car.Id, "AA1234BB", "Skoda Fabia", "Economy", 800m, 10_000, "Available"), dto);
        Assert.Equal(new CarResponse(car.Id, "Skoda Fabia (AA1234BB)", "AA1234BB", "Economy", 800m, 10_000, "Available"), response);
    }

    [Fact]
    public void Details_contain_nested_car_renter_period_and_money()
    {
        var rental = TestData.OpenRental();
        var renter = TestData.Renter();
        var car = new CarSummaryDto(rental.CarId, "AA1234BB", "Skoda Fabia");
        var dto = Mapper.Map<RentalDetailsDto>(new RentalDetailsSource(rental, renter, car));
        Assert.Equal(rental.Id, dto.Id);
        Assert.Equal(car, dto.Car);
        Assert.Equal(new RenterSummaryDto(renter.Id, renter.FullName, renter.Email), dto.Renter);
        Assert.Equal(new PeriodDto(rental.Period.Start, rental.Period.End, 3), dto.Period);
        Assert.Equal(2400m, dto.Total.Amount);
        Assert.Equal("Active", dto.Status);
    }

    [Fact]
    public void Collection_is_mapped_to_flat_list_items()
    {
        var first = TestData.OpenRental();
        var second = TestData.OpenRental();
        var items = Mapper.Map<IReadOnlyList<RentalListItemDto>>(new List<Rental> { first, second });
        Assert.Equal([first.Id, second.Id], items.Select(i => i.Id));
        Assert.Equal(first.Period.Start, items[0].PeriodStart);
        Assert.Equal(first.Period.End, items[0].PeriodEnd);
        Assert.Equal(2400m, items[0].TotalAmount);
    }

    [Fact]
    public void Request_and_command_are_mapped_in_both_directions()
    {
        var request = new OpenRentalRequest(Guid.NewGuid(), Guid.NewGuid(), TestData.Today, TestData.Today.AddDays(2));
        var command = Mapper.Map<OpenRentalCommand>(request);
        Assert.Equal(request, Mapper.Map<OpenRentalRequest>(command));
    }

    [Fact]
    public void Reverse_map_to_a_domain_value_object_goes_through_its_invariants()
    {
        var period = TestData.Period();
        Assert.Equal(period, Mapper.Map<RentalPeriod>(Mapper.Map<PeriodDto>(period)));

        var invalid = new PeriodDto(TestData.Today.AddDays(5), TestData.Today, 0);
        var error = Assert.ThrowsAny<Exception>(() => Mapper.Map<RentalPeriod>(invalid));
        Assert.IsType<DomainRuleException>(error.GetBaseException());
    }

    [Fact]
    public void Response_json_does_not_expose_internal_fields()
    {
        var json = JsonSerializer.Serialize(Mapper.Map<RentalCreatedDto>(TestData.OpenRental()));
        Assert.DoesNotContain("Version", json);
        Assert.DoesNotContain("RenterId", json);
    }

    [Fact]
    public void Registered_renter_response_hides_license_and_birth_date()
    {
        var names = typeof(RenterRegisteredDto).GetProperties().Select(p => p.Name).ToArray();
        Assert.Equal(["Id", "FullName", "Email"], names);
    }

    [Fact]
    public void Controllers_do_not_use_domain_types_in_their_signatures()
    {
        var domain = typeof(Entity).Assembly;
        var actions = typeof(CarsController).Assembly.GetTypes()
            .Where(t => typeof(ControllerBase).IsAssignableFrom(t))
            .SelectMany(t => t.GetMethods(BindingFlags.Public | BindingFlags.Instance | BindingFlags.DeclaredOnly));
        foreach (var action in actions)
        {
            var types = action.GetParameters().Select(p => p.ParameterType).Append(action.ReturnType);
            Assert.DoesNotContain(types.SelectMany(Unwrap), t => t.Assembly == domain);
        }
    }

    private static IEnumerable<Type> Unwrap(Type type) =>
        type.IsGenericType ? type.GetGenericArguments().SelectMany(Unwrap) : [type];
}
