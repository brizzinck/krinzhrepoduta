using CarRental.Application.Common.Audit;
using CarRental.Application.Common.Events;
using CarRental.Application.Fleet;
using CarRental.Application.Fleet.Handlers;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.Handlers;
using CarRental.Application.Rentals.Pricing;
using CarRental.Application.Rentals.UseCases;
using CarRental.Infrastructure.Audit;
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
        var audit = new AuditLogHandler(new FileAuditLog(AuditPath));
        bus.Subscribe<RentalOpened>(new NotifyRenterOnRentalOpened(new SqliteRenterStore(db), Notifications));
        bus.Subscribe<RentalOpened>(new TrackRentalOnRentalOpened(Analytics));
        bus.Subscribe<RentalOpened>(audit);
        bus.Subscribe<RentalCompleted>(new UpdateCarMileageOnRentalCompleted(new SqliteCarStore(db)));
        bus.Subscribe<RentalCompleted>(audit);
        bus.Subscribe<RentalCancelled>(audit);
        return bus;
    }

    /// <summary>Сценарії оренди, зібрані як у застосунку (ті самі правила тарифу).</summary>
    public RentalApi Rentals(CarRentalDbContext db, IEventBus? bus = null, IEnumerable<IPricingRule>? rules = null)
    {
        var store = new SqliteRentalStore(db);
        bus ??= BuildBus(db);
        var pricing = new PricingService(rules ?? [new ClassMultiplierRule(), new LongRentalDiscountRule()]);
        return new RentalApi(
            new OpenRentalHandler(store, new SqliteRenterStore(db), new FleetGateway(new SqliteCarStore(db)), pricing, bus),
            new CompleteRentalHandler(store, bus),
            new CancelRentalHandler(store, bus),
            new RentalQueryService(store));
    }

    public void Dispose()
    {
        _connection.Dispose();
        if (File.Exists(AuditPath))
            File.Delete(AuditPath);
    }
}

/// <summary>Зручний фасад над чотирма сценаріями: тести викликають їх як єдиний API.</summary>
public sealed class RentalApi(OpenRentalHandler open, CompleteRentalHandler complete,
    CancelRentalHandler cancel, RentalQueryService queries)
{
    public Task<RentalDto> Open(OpenRentalCommand cmd) => open.HandleAsync(cmd);
    public Task<RentalDto> Complete(Guid id, int endMileage) => complete.HandleAsync(new CompleteRentalCommand(id, endMileage));
    public Task<RentalDto> Cancel(Guid id) => cancel.HandleAsync(new CancelRentalCommand(id));
    public Task<RentalDto> Get(Guid id) => queries.Get(id);
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
