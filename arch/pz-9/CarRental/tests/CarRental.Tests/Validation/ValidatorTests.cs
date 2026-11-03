using CarRental.Application.Common.Events;
using CarRental.Application.Fleet.Dto;
using CarRental.Application.Rentals.Pricing;
using CarRental.Application.Rentals.UseCases;
using CarRental.Application.Validation;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;
using CarRental.Infrastructure.Integration;
using CarRental.Tests.Fakes;
using CarRental.Tests.Integration;
using CarRental.Tests.Mapping;
using CarRental.WebApi;
using CarRental.WebApi.Validation;
using FluentValidation.TestHelper;

namespace CarRental.Tests.Validation;

public class ValidatorTests
{
    private static readonly DateOnly Today = new(2030, 6, 1);
    private static readonly FakeClock Clock = new(new DateTime(2030, 6, 1, 0, 0, 0, DateTimeKind.Utc));

    private sealed class NoBus : IEventBus
    {
        public int Published { get; private set; }
        public Task PublishAsync<TEvent>(TEvent evt, CancellationToken ct)
        {
            Published++;
            return Task.CompletedTask;
        }
    }

    private static Renter NewRenter(DateOnly birth, DateOnly licenseUntil) =>
        Renter.Register("Іваненко Іван Іванович", "ivan@example.com", birth, new DriverLicense("ABC123456", licenseUntil), Today);

    private static Car NewCar(string plate = "AA0001BB") =>
        Car.Register(new LicensePlate(plate), "Skoda Fabia", CarClass.Economy, new Money(800m), 10_000);

    private static OpenRentalCommandValidator OpenValidator(InMemoryUnitOfWork uow) =>
        new(uow, new FleetGateway(uow.Cars, TestMapper.Instance), Clock);

    [Fact]
    public void Open_request_reports_all_format_errors_at_once()
    {
        var result = new OpenRentalRequestValidator().TestValidate(
            new OpenRentalRequest(Guid.Empty, Guid.Empty, Today.AddDays(5), Today.AddDays(1)));
        result.ShouldHaveValidationErrorFor(x => x.CarId);
        result.ShouldHaveValidationErrorFor(x => x.RenterId);
        result.ShouldHaveValidationErrorFor(x => x.End);
    }

    [Theory]
    [InlineData(30, true)]
    [InlineData(31, false)]
    public void Open_request_allows_at_most_30_days(int days, bool valid)
    {
        var result = new OpenRentalRequestValidator().TestValidate(
            new OpenRentalRequest(Guid.NewGuid(), Guid.NewGuid(), Today, Today.AddDays(days)));
        Assert.Equal(valid, result.IsValid);
    }

    [Theory]
    [InlineData("AA1234BB", true)]
    [InlineData("aa1234bb", true)]
    [InlineData("123", false)]
    [InlineData("", false)]
    public void Car_request_checks_plate_format(string plate, bool valid)
    {
        var result = new RegisterCarRequestValidator().TestValidate(new RegisterCarRequest(plate, "Skoda", "Economy", 800m, 0));
        Assert.Equal(valid, result.IsValid);
    }

    [Fact]
    public void Car_request_reports_rate_mileage_and_model()
    {
        var result = new RegisterCarRequestValidator().TestValidate(new RegisterCarRequest("AA1234BB", "", "Economy", -5m, -1));
        result.ShouldHaveValidationErrorFor(x => x.Model);
        result.ShouldHaveValidationErrorFor(x => x.DailyRate);
        result.ShouldHaveValidationErrorFor(x => x.Mileage);
    }

    [Fact]
    public void Renter_request_checks_name_email_and_license()
    {
        var result = new RegisterRenterRequestValidator().TestValidate(
            new RegisterRenterRequest("", "not-mail", default, "1", default));
        result.ShouldHaveValidationErrorFor(x => x.FullName);
        result.ShouldHaveValidationErrorFor(x => x.Email);
        result.ShouldHaveValidationErrorFor(x => x.BirthDate);
        result.ShouldHaveValidationErrorFor(x => x.LicenseNumber);
        result.ShouldHaveValidationErrorFor(x => x.LicenseValidUntil);
    }

    [Fact]
    public void Complete_request_rejects_negative_mileage() =>
        new CompleteRentalRequestValidator().TestValidate(new CompleteRentalRequest(-1))
            .ShouldHaveValidationErrorFor(x => x.EndMileage);

    [Fact]
    public async Task Young_renter_and_expired_license_are_both_reported_using_the_database()
    {
        var uow = new InMemoryUnitOfWork();
        var car = NewCar();
        var renter = NewRenter(Today.AddYears(-20), Today.AddDays(2));
        await uow.Cars.AddAsync(car, default);
        await uow.Renters.AddAsync(renter, default);

        var result = await OpenValidator(uow).TestValidateAsync(
            new OpenRentalCommand(car.Id, renter.Id, Today.AddDays(1), Today.AddDays(4)));

        var errors = result.ShouldHaveValidationErrorFor(x => x.RenterId);
        Assert.Equal(2, errors.Count());
    }

    [Fact]
    public async Task Car_in_maintenance_is_reported()
    {
        var uow = new InMemoryUnitOfWork();
        var car = NewCar();
        car.SendToMaintenance();
        await uow.Cars.AddAsync(car, default);
        var result = await OpenValidator(uow).TestValidateAsync(
            new OpenRentalCommand(car.Id, Guid.NewGuid(), Today.AddDays(1), Today.AddDays(4)));
        result.ShouldHaveValidationErrorFor(x => x.CarId);
    }

    [Fact]
    public async Task Missing_car_and_renter_are_left_for_the_scenario_to_report_as_404()
    {
        var result = await OpenValidator(new InMemoryUnitOfWork()).TestValidateAsync(
            new OpenRentalCommand(Guid.NewGuid(), Guid.NewGuid(), Today.AddDays(1), Today.AddDays(4)));
        Assert.True(result.IsValid);
    }

    [Fact]
    public async Task Start_in_the_past_is_reported()
    {
        var result = await OpenValidator(new InMemoryUnitOfWork()).TestValidateAsync(
            new OpenRentalCommand(Guid.NewGuid(), Guid.NewGuid(), Today.AddDays(-1), Today.AddDays(2)));
        result.ShouldHaveValidationErrorFor(x => x.Start);
    }

    [Fact]
    public async Task Duplicate_plate_and_unknown_class_are_reported()
    {
        var uow = new InMemoryUnitOfWork();
        await uow.Cars.AddAsync(NewCar("AA0001BB"), default);
        var result = await new RegisterCarCommandValidator(uow).TestValidateAsync(
            new RegisterCarCommand("aa0001bb", "Skoda", "Bus", 800m, 0));
        result.ShouldHaveValidationErrorFor(x => x.Plate);
        result.ShouldHaveValidationErrorFor(x => x.Class);
    }

    [Fact]
    public async Task Plate_with_wrong_format_is_not_a_uniqueness_error_and_does_not_throw()
    {
        var result = await new RegisterCarCommandValidator(new InMemoryUnitOfWork()).TestValidateAsync(
            new RegisterCarCommand("123", "Skoda", "Economy", 800m, 0));
        result.ShouldNotHaveValidationErrorFor(x => x.Plate);
    }

    [Fact]
    public void Birth_date_in_the_future_is_reported() =>
        new RegisterRenterCommandValidator(Clock).TestValidate(
                new RegisterRenterCommand("Іван", "i@example.com", Today.AddDays(1), "ABC123456", Today.AddYears(1)))
            .ShouldHaveValidationErrorFor(x => x.BirthDate);

    [Fact]
    public void Complete_and_cancel_commands_require_a_rental()
    {
        new CompleteRentalCommandValidator().TestValidate(new CompleteRentalCommand(Guid.Empty, 10))
            .ShouldHaveValidationErrorFor(x => x.RentalId);
        new CancelRentalCommandValidator().TestValidate(new CancelRentalCommand(Guid.Empty))
            .ShouldHaveValidationErrorFor(x => x.RentalId);
    }

    [Fact]
    public async Task Failed_validation_runs_no_business_logic()
    {
        var uow = new InMemoryUnitOfWork();
        var car = NewCar();
        var renter = NewRenter(Today.AddYears(-20), Today.AddYears(5));
        await uow.Cars.AddAsync(car, default);
        await uow.Renters.AddAsync(renter, default);
        var bus = new NoBus();
        var gateway = new FleetGateway(uow.Cars, TestMapper.Instance);
        var handler = new OpenRentalHandler(uow, gateway, new PricingService([new ClassMultiplierRule()]),
            Clock, TestMapper.Instance, bus, new OpenRentalCommandValidator(uow, gateway, Clock));

        await Assert.ThrowsAsync<ValidationException>(() =>
            handler.HandleAsync(new OpenRentalCommand(car.Id, renter.Id, Today.AddDays(1), Today.AddDays(4))));

        Assert.Equal(0, uow.SaveCount);
        Assert.Equal(0, bus.Published);
        Assert.Empty(await uow.Rentals.GetAllAsync(default));
    }

    [Fact]
    public void Domain_keeps_its_invariants_without_any_validator()
    {
        var young = NewRenter(Today.AddYears(-20), Today.AddYears(5));
        var car = new RentableCar(Guid.NewGuid(), new Money(800m), CarClass.Economy, 0, true);
        var period = new RentalPeriod(Today.AddDays(1), Today.AddDays(4));
        Assert.Throws<DomainRuleException>(() => Rental.Open(car, young, period, new Money(2400m), Today, []));
        Assert.Throws<DomainRuleException>(() => new RentalPeriod(Today, Today.AddDays(31)));
    }
}
