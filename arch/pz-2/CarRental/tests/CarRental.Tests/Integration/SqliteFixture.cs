using CarRental.Application.Common.Audit;
using CarRental.Application.Common.Events;
using CarRental.Application.Fleet;
using CarRental.Application.Fleet.Handlers;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.Handlers;
using CarRental.Infrastructure.Integration;
using CarRental.Infrastructure.Persistence;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Tests.Integration;

/// <summary>
/// SQLite у пам'яті: з'єднання тримається відкритим, тож кілька DbContext бачать одну БД,
/// що імітує окремі запити до того самого сховища.
/// </summary>
public sealed class SqliteFixture : IDisposable
{
    private readonly SqliteConnection _connection = new("Data Source=:memory:");
    private readonly DbContextOptions<CarRentalDbContext> _options;

    public SqliteFixture()
    {
        _connection.Open();
        _options = new DbContextOptionsBuilder<CarRentalDbContext>().UseSqlite(_connection).Options;
        using var db = NewContext();
        db.Database.EnsureCreated();
    }

    public CarRentalDbContext NewContext() => new(_options);

    public CarService Cars(CarRentalDbContext db) => new(new SqliteCarStore(db));
    public RenterService Renters(CarRentalDbContext db) => new(new SqliteRenterStore(db));

    public RecordingNotifications Notifications { get; } = new();
    public RecordingAnalytics Analytics { get; } = new();
    public string AuditPath { get; } = Path.Combine(Path.GetTempPath(), $"carrental-audit-{Guid.NewGuid():N}.log");

    /// <summary>Шина з тим самим набором підписників, що й у застосунку (див. CompositionRoot у WebApi).</summary>
    public InProcessEventBus BuildBus(CarRentalDbContext db)
    {
        var bus = new InProcessEventBus();
        var audit = new AuditLogHandler(AuditPath);
        bus.Subscribe<RentalOpened>(new NotifyRenterOnRentalOpened(new SqliteRenterStore(db), Notifications));
        bus.Subscribe<RentalOpened>(new TrackRentalOnRentalOpened(Analytics));
        bus.Subscribe<RentalOpened>(audit);
        bus.Subscribe<RentalCompleted>(new UpdateCarMileageOnRentalCompleted(new SqliteCarStore(db)));
        bus.Subscribe<RentalCompleted>(audit);
        bus.Subscribe<RentalCancelled>(audit);
        return bus;
    }

    public RentalService Rentals(CarRentalDbContext db, IEventBus? bus = null) =>
        new(new SqliteRentalStore(db), new SqliteRenterStore(db), new FleetGateway(new SqliteCarStore(db)),
            bus ?? BuildBus(db));

    public void Dispose()
    {
        _connection.Dispose();
        if (File.Exists(AuditPath))
            File.Delete(AuditPath);
    }
}

public sealed class RecordingNotifications : INotificationService
{
    public List<string> Sent { get; } = [];
    public Task SendRentalOpenedAsync(Guid rentalId, string renterEmail, CancellationToken ct)
    {
        Sent.Add(renterEmail);
        return Task.CompletedTask;
    }
}

public sealed class RecordingAnalytics : IAnalyticsService
{
    public List<decimal> Totals { get; } = [];
    public Task TrackRentalOpenedAsync(Guid rentalId, decimal total, CancellationToken ct)
    {
        Totals.Add(total);
        return Task.CompletedTask;
    }
}
