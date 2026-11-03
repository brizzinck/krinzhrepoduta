using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Rentals.Handlers;

/// <summary>Реакція «повідомити орендаря»: подія містить лише Id, адресу береться з репозиторію.</summary>
public sealed class NotifyRenterOnRentalOpened(IUnitOfWork uow, INotificationService notifications)
    : IEventHandler<RentalOpened>
{
    public async Task HandleAsync(RentalOpened evt, CancellationToken ct)
    {
        var renter = await uow.Renters.GetByIdAsync(evt.RenterId, ct);
        if (renter is not null)
            await notifications.SendRentalOpenedAsync(evt.RentalId, renter.Email, ct);
    }
}
