using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Rentals.UseCases;

public sealed record CompleteRentalCommand(Guid RentalId, int EndMileage);

/// <summary>Сценарій «завершити оренду».</summary>
public sealed class CompleteRentalHandler(IRentalStore rentals, IRentalMapper mapper, IEventBus events)
{
    public async Task<RentalDto> HandleAsync(CompleteRentalCommand cmd, CancellationToken ct = default)
    {
        var rental = await rentals.Find(cmd.RentalId, ct) ?? throw new NotFoundException("Оренду");
        rental.Complete(cmd.EndMileage);
        await rentals.Save(ct);

        await events.PublishAsync(new RentalCompleted(rental.Id, rental.CarId, cmd.EndMileage), ct);
        return mapper.ToDto(rental);
    }
}
