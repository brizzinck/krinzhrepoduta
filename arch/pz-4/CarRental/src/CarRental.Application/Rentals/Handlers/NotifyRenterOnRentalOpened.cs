using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Rentals.Handlers;

/// <summary>Реакція «повідомити орендаря»: подія містить лише Id, адресу береться зі сховища.</summary>
public sealed class NotifyRenterOnRentalOpened(IRenterStore renters, INotificationService notifications)
    : IEventHandler<RentalOpened>
{
    public async Task HandleAsync(RentalOpened evt, CancellationToken ct)
    {
        var renter = await renters.Find(evt.RenterId, ct);
        if (renter is not null)
            await notifications.SendRentalOpenedAsync(evt.RentalId, renter.Email, ct);
    }
}
