using CarRental.Application.Rentals;
using Microsoft.Extensions.Logging;

namespace CarRental.Infrastructure.Notifications;

/// <summary>Лічильники прокату в пам'яті процесу (скидаються при перезапуску).</summary>
public sealed class InMemoryAnalyticsService(ILogger<InMemoryAnalyticsService> log) : IAnalyticsService
{
    private readonly object _gate = new();
    private int _opened;
    private decimal _revenue;

    public Task TrackRentalOpenedAsync(Guid rentalId, decimal total, CancellationToken ct)
    {
        int opened;
        decimal revenue;
        lock (_gate)
        {
            opened = ++_opened;
            revenue = _revenue += total;
        }
        log.LogInformation("Аналітика: оренду {RentalId} враховано, усього оренд {Opened}, виручка {Revenue:0.00} UAH.",
            rentalId, opened, revenue);
        return Task.CompletedTask;
    }
}
