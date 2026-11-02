using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Rentals.UseCases;

public sealed record CancelRentalCommand(Guid RentalId);

/// <summary>Сценарій «скасувати оренду».</summary>
public sealed class CancelRentalHandler(IRentalStore rentals, IClock clock, IRentalMapper mapper, IEventBus events)
{
    public async Task<RentalDto> HandleAsync(CancelRentalCommand cmd, CancellationToken ct = default)
    {
        var rental = await rentals.Find(cmd.RentalId, ct) ?? throw new NotFoundException("Оренду");
        rental.Cancel(clock.Today);
        await rentals.Save(ct);

        await events.PublishAsync(new RentalCancelled(rental.Id, rental.CarId), ct);
        return mapper.ToDto(rental);
    }
}
