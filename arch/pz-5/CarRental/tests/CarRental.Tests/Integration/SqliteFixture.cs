using CarRental.Application;
using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Pricing;
using CarRental.Application.Rentals.UseCases;
using CarRental.Infrastructure;
using CarRental.Infrastructure.Persistence;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace CarRental.Tests.Integration;

/// <summary>Годинник для тестів: час задається явно й змінюється за потреби.</summary>
public sealed class FakeClock(DateTime utcNow) : IClock
{
    public DateTime UtcNow { get; set; } = utcNow;
    public DateOnly Today => DateOnly.FromDateTime(UtcNow);
}

/// <summary>
/// Складання, як у застосунку: ті самі AddApplication та AddInfrastructure, але з SQLite у пам'яті,
/// підробленим годинником, сповіщеннями й аналітикою. З'єднання тримається відкритим, тож кілька
/// запитів (областей контейнера) бачать одну БД, як окремі HTTP-запити до одного сховища.
/// </summary>
public sealed class SqliteFixture : IDisposable
{
    private readonly SqliteConnection _connection = new("Data Source=:memory:");

    public FakeClock Clock { get; } = new(DateTime.UtcNow);
    public RecordingNotifications Notifications { get; } = new();
    public RecordingAnalytics Analytics { get; } = new();
    public string AuditPath { get; } = Path.Combine(Path.GetTempPath(), $"carrental-audit-{Guid.NewGuid():N}.log");
    public ServiceProvider Provider { get; }

    public SqliteFixture(Action<IServiceCollection>? configure = null)
    {
        _connection.Open();
        var services = new ServiceCollection();
        services.AddLogging();
        services.AddApplication().AddInfrastructure(o => o.UseSqlite(_connection), AuditPath);
        // Підміна портів на тестові реалізації: остання реєстрація перемагає.
        services.AddSingleton<IClock>(Clock);
        services.AddSingleton<INotificationService>(Notifications);
        services.AddSingleton<IAnalyticsService>(Analytics);
        configure?.Invoke(services);
        Provider = services.BuildServiceProvider(new ServiceProviderOptions { ValidateScopes = true, ValidateOnBuild = true });
        using var request = NewContext();
        request.Db.Database.EnsureCreated();
    }

    /// <summary>Нова область контейнера: імітує один HTTP-запит.</summary>
    public Request NewContext() => new(Provider.CreateScope());

    public CarService Cars(Request request) => request.Get<CarService>();
    public RenterService Renters(Request request) => request.Get<RenterService>();

    /// <summary>Шина подій застосунку (Singleton), яка створює власну область на публікацію.</summary>
    public IEventBus Bus => Provider.GetRequiredService<IEventBus>();

    /// <summary>
    /// Сценарії оренди із контейнера. Для підміни шини чи правил тарифу сценарії збираються вручну
    /// з решти залежностей тієї самої області (так тести показують, що змінюється лише одна залежність).
    /// </summary>
    public RentalApi Rentals(Request request, IEventBus? bus = null, IEnumerable<IPricingRule>? rules = null)
    {
        if (bus is null && rules is null)
            return new RentalApi(request.Get<OpenRentalHandler>(), request.Get<CompleteRentalHandler>(),
                request.Get<CancelRentalHandler>(), request.Get<RentalQueryService>());

        bus ??= Bus;
        var uow = request.Get<IUnitOfWork>();
        var mapper = request.Get<IRentalMapper>();
        var pricing = rules is null ? request.Get<IPricingService>() : new PricingService(rules);
        return new RentalApi(
            new OpenRentalHandler(uow, request.Get<IFleetGateway>(), pricing, Clock, mapper, bus),
            new CompleteRentalHandler(uow, mapper, bus),
            new CancelRentalHandler(uow, Clock, mapper, bus),
            new RentalQueryService(request.Get<IRentalReader>(), mapper));
    }

    public void Dispose()
    {
        Provider.Dispose();
        _connection.Dispose();
        if (File.Exists(AuditPath))
            File.Delete(AuditPath);
    }
}

/// <summary>Одна область контейнера (запит): дає компоненти й DbContext цього запиту.</summary>
public sealed class Request(IServiceScope scope) : IDisposable, IAsyncDisposable
{
    public T Get<T>() where T : notnull => scope.ServiceProvider.GetRequiredService<T>();
    public CarRentalDbContext Db => Get<CarRentalDbContext>();
    public void Dispose() => scope.Dispose();
    public ValueTask DisposeAsync()
    {
        scope.Dispose();
        return ValueTask.CompletedTask;
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
