using CarRental.Application.Rentals;
using Microsoft.Extensions.Logging;

namespace CarRental.Infrastructure.Notifications;

/// <summary>Навчальна реалізація повідомлень: «лист» орендарю пишеться в журнал.</summary>
public sealed class ConsoleNotificationService(ILogger<ConsoleNotificationService> log) : INotificationService
{
    public Task SendRentalOpenedAsync(Guid rentalId, string renterEmail, CancellationToken ct)
    {
        log.LogInformation("Лист до {Email}: оренду {RentalId} підтверджено.", renterEmail, rentalId);
        return Task.CompletedTask;
    }
}
