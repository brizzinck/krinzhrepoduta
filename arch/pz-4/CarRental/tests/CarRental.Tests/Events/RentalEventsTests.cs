using CarRental.Application.Common.Events;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Events;
using CarRental.Domain.Common;
using CarRental.Tests.Integration;
using Microsoft.Extensions.DependencyInjection;

namespace CarRental.Tests.Events;

public class RentalEventsTests
{
    private static readonly DateOnly Today = DateOnly.FromDateTime(DateTime.UtcNow);

    /// <summary>Шина-реєстратор: запам'ятовує опубліковані події, обробників не викликає.</summary>
    private sealed class RecordingBus : IEventBus
    {
        public List<object> Published { get; } = [];
        public Task PublishAsync<TEvent>(TEvent evt, CancellationToken ct)
        {
            Published.Add(evt!);
            return Task.CompletedTask;
        }
    }

    private static async Task<(Guid car, Guid renter)> SeedAsync(SqliteFixture f)
    {
        await using var db = f.NewContext();
        var car = await f.Cars(db).Register("AA1234BB", "Skoda Fabia", CarClass.Economy, 800m, 10_000);
        var renter = await f.Renters(db).Register("Іваненко Іван Іванович", "ivan@example.com",
            new DateOnly(1990, 5, 20), "abc123456", Today.AddYears(5));
        return (car.Id, renter.Id);
    }

    [Fact]
    public async Task Open_publishes_RentalOpened_with_minimal_data_after_saving()
    {
        using var f = new SqliteFixture();
        var (car, renter) = await SeedAsync(f);
        var bus = new RecordingBus();
        await using var db = f.NewContext();
        var rental = await f.Rentals(db, bus).Open(new OpenRentalCommand(car, renter, Today.AddDays(1), Today.AddDays(4)));

        var evt = Assert.IsType<RentalOpened>(Assert.Single(bus.Published));
        Assert.Equal(new RentalOpened(rental.Id, car, renter, Today.AddDays(1), Today.AddDays(4), 2400m), evt);
    }

    [Fact]
    public async Task Complete_and_Cancel_publish_their_events()
    {
        using var f = new SqliteFixture();
        var (car, renter) = await SeedAsync(f);
        var bus = new RecordingBus();
        await using var db = f.NewContext();
        var service = f.Rentals(db, bus);
        var first = await service.Open(new OpenRentalCommand(car, renter, Today.AddDays(1), Today.AddDays(3)));
        var second = await service.Open(new OpenRentalCommand(car, renter, Today.AddDays(5), Today.AddDays(7)));
        bus.Published.Clear();

        await service.Complete(first.Id, 10_300);
        await service.Cancel(second.Id);

        Assert.Equal(new object[]
        {
            new RentalCompleted(first.Id, car, 10_300),
            new RentalCancelled(second.Id, car)
        }, bus.Published);
    }

    [Fact]
    public async Task Rejected_operation_publishes_nothing()
    {
        using var f = new SqliteFixture();
        var (car, renter) = await SeedAsync(f);
        var bus = new RecordingBus();
        await using var db = f.NewContext();
        var service = f.Rentals(db, bus);
        await service.Open(new OpenRentalCommand(car, renter, Today.AddDays(1), Today.AddDays(4)));
        bus.Published.Clear();

        await Assert.ThrowsAsync<DomainConflictException>(() =>
            service.Open(new OpenRentalCommand(car, renter, Today.AddDays(2), Today.AddDays(5))));
        Assert.Empty(bus.Published);
    }

    [Theory]
    [InlineData(typeof(CarRental.Application.Rentals.UseCases.OpenRentalHandler))]
    [InlineData(typeof(CarRental.Application.Rentals.UseCases.CompleteRentalHandler))]
    [InlineData(typeof(CarRental.Application.Rentals.UseCases.CancelRentalHandler))]
    public void Rental_use_cases_do_not_depend_on_consumers_of_their_events(Type useCase)
    {
        var dependencies = useCase.GetConstructors().Single().GetParameters().Select(p => p.ParameterType).ToArray();
        Assert.DoesNotContain(typeof(INotificationService), dependencies);
        Assert.DoesNotContain(typeof(IAnalyticsService), dependencies);
        Assert.Contains(typeof(IEventBus), dependencies);
    }

    [Fact]
    public async Task One_event_triggers_notification_analytics_and_audit()
    {
        using var f = new SqliteFixture();
        var (car, renter) = await SeedAsync(f);
        await using var db = f.NewContext();
        await f.Rentals(db).Open(new OpenRentalCommand(car, renter, Today.AddDays(1), Today.AddDays(4)));

        Assert.Equal(["ivan@example.com"], f.Notifications.Sent);
        Assert.Equal([2400m], f.Analytics.Totals);
        Assert.Contains("RentalOpened", await File.ReadAllTextAsync(f.AuditPath));
    }

    [Fact]
    public async Task New_subscriber_is_added_without_changing_RentalService()
    {
        var extra = new List<string>();
        // Другий споживач тієї самої події додається лише реєстрацією в контейнері.
        using var f = new SqliteFixture(services =>
            services.AddScoped<IEventHandler<RentalOpened>>(_ => new ExtraHandler(extra)));
        var (car, renter) = await SeedAsync(f);
        await using var db = f.NewContext();
        await f.Rentals(db).Open(new OpenRentalCommand(car, renter, Today.AddDays(1), Today.AddDays(4)));

        Assert.Single(extra);
        Assert.Equal(["ivan@example.com"], f.Notifications.Sent); // попередні споживачі працюють як і раніше
    }

    private sealed class ExtraHandler(List<string> log) : IEventHandler<RentalOpened>
    {
        public Task HandleAsync(RentalOpened evt, CancellationToken ct)
        {
            log.Add($"extra:{evt.RentalId}");
            return Task.CompletedTask;
        }
    }

    [Fact]
    public async Task Fleet_updates_car_mileage_in_reaction_to_RentalCompleted()
    {
        using var f = new SqliteFixture();
        var (car, renter) = await SeedAsync(f);
        Guid id;
        await using (var db = f.NewContext())
            id = (await f.Rentals(db).Open(new OpenRentalCommand(car, renter, Today.AddDays(1), Today.AddDays(3)))).Id;
        await using (var db = f.NewContext())
            await f.Rentals(db).Complete(id, 10_480);

        await using var check = f.NewContext();
        Assert.Equal(10_480, (await f.Cars(check).Get(car)).Mileage);
    }

    [Fact]
    public async Task Stale_mileage_from_event_does_not_decrease_car_mileage()
    {
        using var f = new SqliteFixture();
        var (car, _) = await SeedAsync(f);
        await f.Bus.PublishAsync(new RentalCompleted(Guid.NewGuid(), car, 10_900), default);
        await f.Bus.PublishAsync(new RentalCompleted(Guid.NewGuid(), car, 10_500), default);
        await using var db = f.NewContext();
        Assert.Equal(10_900, (await f.Cars(db).Get(car)).Mileage);
    }

    [Fact]
    public async Task Audit_log_gets_a_line_for_each_of_the_three_events()
    {
        using var f = new SqliteFixture();
        var (car, renter) = await SeedAsync(f);
        await using var db = f.NewContext();
        var service = f.Rentals(db);
        var first = await service.Open(new OpenRentalCommand(car, renter, Today.AddDays(1), Today.AddDays(3)));
        var second = await service.Open(new OpenRentalCommand(car, renter, Today.AddDays(5), Today.AddDays(7)));
        await service.Complete(first.Id, 10_300);
        await service.Cancel(second.Id);

        var lines = await File.ReadAllLinesAsync(f.AuditPath);
        Assert.Equal(4, lines.Length); // дві відкритих + завершена + скасована
        Assert.Contains(lines, l => l.Contains("RentalCompleted"));
        Assert.Contains(lines, l => l.Contains("RentalCancelled"));
    }
}
