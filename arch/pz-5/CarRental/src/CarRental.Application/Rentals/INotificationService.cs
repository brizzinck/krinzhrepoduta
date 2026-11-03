namespace CarRental.Application.Rentals;

/// <summary>Повідомлення орендарю. Реалізація (пошта, SMS, журнал) лежить в Infrastructure.</summary>
public interface INotificationService
{
    Task SendRentalOpenedAsync(Guid rentalId, string renterEmail, CancellationToken ct);
}
