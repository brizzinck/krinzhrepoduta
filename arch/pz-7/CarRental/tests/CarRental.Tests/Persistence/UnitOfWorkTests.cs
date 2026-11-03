using System.Reflection;
using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.UseCases;
using CarRental.Application.Rentals.Pricing;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;
using CarRental.Infrastructure.Integration;
using CarRental.Tests.Fakes;
using CarRental.Tests.Integration;
using CarRental.Tests.Mapping;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Tests.Persistence;

public class UnitOfWorkTests
{
    private static readonly DateOnly Today = DateOnly.FromDateTime(DateTime.UtcNow);

    private static Car NewCar(string plate) =>
        Car.Register(new LicensePlate(plate), "Skoda Fabia", CarClass.Economy, new Money(800m), 10_000);

    private static Renter NewRenter() =>
        Renter.Register("Іваненко Іван Іванович", "ivan@example.com", new DateOnly(1990, 5, 20),
            new DriverLicense("ABC123456", Today.AddYears(5)), Today);

    [Fact]
    public async Task Changes_of_several_repositories_are_stored_by_one_save()
    {
        using var f = new SqliteFixture();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            await uow.Cars.AddAsync(NewCar("AA0001BB"), default);
            await uow.Renters.AddAsync(NewRenter(), default);
            Assert.Equal(2, await uow.SaveChangesAsync(default));
        }
        await using var check = f.NewContext();
        var uow2 = check.Get<IUnitOfWork>();
        Assert.Single(await uow2.Cars.GetAllAsync(default));
        Assert.Single(await uow2.Renters.GetAllAsync(default));
    }

    [Fact]
    public async Task Nothing_is_stored_until_SaveChanges_is_called()
    {
        using var f = new SqliteFixture();
        await using (var request = f.NewContext())
            await request.Get<IUnitOfWork>().Cars.AddAsync(NewCar("AA0001BB"), default); // без SaveChanges
        await using var check = f.NewContext();
        Assert.Empty(await check.Get<IUnitOfWork>().Cars.GetAllAsync(default));
    }

    [Fact]
    public async Task Failure_in_the_middle_of_an_operation_leaves_nothing_in_the_database()
    {
        using var f = new SqliteFixture();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            await uow.Cars.AddAsync(NewCar("AA0001BB"), default);
            await uow.Renters.AddAsync(NewRenter(), default);
            // Бізнес-правило порушується після підготовки змін, але до фіксації: виняток, SaveChanges не викликається.
            Assert.Throws<DomainRuleException>(() => NewCar("not-a-plate"));
        }
        await using var check = f.NewContext();
        var uow2 = check.Get<IUnitOfWork>();
        Assert.Empty(await uow2.Cars.GetAllAsync(default));
        Assert.Empty(await uow2.Renters.GetAllAsync(default));
    }

    [Fact]
    public async Task Database_failure_during_save_rolls_back_all_repositories()
    {
        using var f = new SqliteFixture();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            await uow.Renters.AddAsync(NewRenter(), default);
            await uow.Cars.AddAsync(NewCar("AA0001BB"), default);
            await uow.Cars.AddAsync(NewCar("AA0001BB"), default); // дубль унікального номера порушить обмеження БД
            await Assert.ThrowsAsync<DbUpdateException>(() => uow.SaveChangesAsync(default));
        }
        await using var check = f.NewContext();
        var uow2 = check.Get<IUnitOfWork>();
        Assert.Empty(await uow2.Cars.GetAllAsync(default));
        Assert.Empty(await uow2.Renters.GetAllAsync(default)); // орендар теж не збережений: транзакція одна
    }

    [Fact]
    public async Task Concurrent_change_is_reported_as_ConcurrentChangeException_by_the_unit_of_work()
    {
        using var f = new SqliteFixture();
        var car = NewCar("AA0001BB");
        await using (var request = f.NewContext())
        {
            await request.Get<IUnitOfWork>().Cars.AddAsync(car, default);
            await request.Get<IUnitOfWork>().SaveChangesAsync(default);
        }
        await using var first = f.NewContext();
        await using var second = f.NewContext();
        var a = (await first.Get<IUnitOfWork>().Cars.GetByIdAsync(car.Id, default))!;
        var b = (await second.Get<IUnitOfWork>().Cars.GetByIdAsync(car.Id, default))!;
        a.RecordMileage(10_100);
        b.SendToMaintenance();
        await first.Get<IUnitOfWork>().SaveChangesAsync(default);
        await Assert.ThrowsAsync<ConcurrentChangeException>(() => second.Get<IUnitOfWork>().SaveChangesAsync(default));
    }

    [Fact]
    public void Application_and_Domain_do_not_reference_the_orm()
    {
        foreach (var assembly in new[] { typeof(IUnitOfWork).Assembly, typeof(Car).Assembly })
            Assert.DoesNotContain(assembly.GetReferencedAssemblies(),
                a => a.Name!.StartsWith("Microsoft.EntityFrameworkCore", StringComparison.Ordinal));
    }

    [Fact]
    public void Scenarios_use_only_the_unit_of_work_and_never_the_repositories_or_dbcontext_directly()
    {
        var scenarios = typeof(IUnitOfWork).Assembly.GetTypes()
            .Where(t => t.IsPublic && t is { IsClass: true } && (t.Name.EndsWith("Handler") || t.Name.EndsWith("Service")
                || t.Name.Contains("On")) && t.GetConstructors().Length == 1);
        foreach (var type in scenarios)
        {
            var dependencies = type.GetConstructors().Single().GetParameters().Select(p => p.ParameterType).ToArray();
            Assert.DoesNotContain(dependencies, d => d == typeof(ICarRepository) || d == typeof(IRenterRepository) || d == typeof(IRentalRepository));
            Assert.DoesNotContain(dependencies, d => d.Name.EndsWith("DbContext"));
        }
        Assert.Contains(typeof(IUnitOfWork), typeof(OpenRentalHandler).GetConstructors().Single().GetParameters().Select(p => p.ParameterType));
    }

    private sealed class RecordingBus : IEventBus
    {
        public List<object> Published { get; } = [];
        public Task PublishAsync<TEvent>(TEvent evt, CancellationToken ct)
        {
            Published.Add(evt!);
            return Task.CompletedTask;
        }
    }

    [Fact]
    public async Task Open_scenario_runs_on_in_memory_repositories_without_any_database()
    {
        var uow = new InMemoryUnitOfWork();
        var car = NewCar("AA0001BB");
        var renter = NewRenter();
        await uow.Cars.AddAsync(car, default);
        await uow.Renters.AddAsync(renter, default);
        var bus = new RecordingBus();
        var handler = new OpenRentalHandler(uow, new FleetGateway(uow.Cars, TestMapper.Instance),
            new PricingService([new ClassMultiplierRule(), new LongRentalDiscountRule()]),
            new FakeClock(DateTime.UtcNow), TestMapper.Instance, bus);

        var dto = await handler.HandleAsync(new OpenRentalCommand(car.Id, renter.Id, Today.AddDays(1), Today.AddDays(4)));

        Assert.Equal(2400m, dto.Total.Amount);
        Assert.Equal(1, uow.SaveCount); // одна фіксація на всю операцію
        Assert.Single(await uow.RentalRepository.GetAllAsync(default));
        Assert.IsType<RentalOpened>(Assert.Single(bus.Published));

        // Відмова за правилами домену не доходить до фіксації й не публікує подій.
        await Assert.ThrowsAsync<DomainConflictException>(() =>
            handler.HandleAsync(new OpenRentalCommand(car.Id, renter.Id, Today.AddDays(2), Today.AddDays(5))));
        Assert.Equal(1, uow.SaveCount);
        Assert.Single(bus.Published);
    }
}
