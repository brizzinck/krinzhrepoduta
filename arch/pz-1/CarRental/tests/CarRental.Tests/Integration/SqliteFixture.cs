using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
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

    public RentalService Rentals(CarRentalDbContext db)
    {
        var cars = new SqliteCarStore(db);
        return new RentalService(new SqliteRentalStore(db), new SqliteRenterStore(db), new FleetGateway(cars),
            new RecordingNotifications(), new RecordingAnalytics());
    }

    public void Dispose() => _connection.Dispose();
}

internal sealed class RecordingNotifications : INotificationService
{
    public List<string> Sent { get; } = [];
    public Task SendRentalOpenedAsync(Guid rentalId, string renterEmail, CancellationToken ct)
    {
        Sent.Add(renterEmail);
        return Task.CompletedTask;
    }
}

internal sealed class RecordingAnalytics : IAnalyticsService
{
    public List<decimal> Totals { get; } = [];
    public Task TrackRentalOpenedAsync(Guid rentalId, decimal total, CancellationToken ct)
    {
        Totals.Add(total);
        return Task.CompletedTask;
    }
}
