namespace CarRental.Application.Rentals;

/// <summary>Збір показників прокату (кількість і виручка).</summary>
public interface IAnalyticsService
{
    Task TrackRentalOpenedAsync(Guid rentalId, decimal total, CancellationToken ct);
}
