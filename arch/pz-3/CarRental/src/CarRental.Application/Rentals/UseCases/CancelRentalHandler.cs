using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Rentals.UseCases;

public sealed record CancelRentalCommand(Guid RentalId);

/// <summary>Сценарій «скасувати оренду».</summary>
public sealed class CancelRentalHandler(IRentalStore rentals, IEventBus events)
{
    public async Task<RentalDto> HandleAsync(CancelRentalCommand cmd, CancellationToken ct = default)
    {
        var rental = await rentals.Find(cmd.RentalId, ct) ?? throw new NotFoundException("Оренду");
        rental.Cancel(DateOnly.FromDateTime(DateTime.UtcNow));
        await rentals.Save(ct);

        await events.PublishAsync(new RentalCancelled(rental.Id, rental.CarId), ct);
        return RentalMapper.ToDto(rental);
    }
}
