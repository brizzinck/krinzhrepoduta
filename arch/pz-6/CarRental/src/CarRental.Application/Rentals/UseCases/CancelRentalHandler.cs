using AutoMapper;
using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Rentals.Dto;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Rentals.UseCases;

public sealed record CancelRentalCommand(Guid RentalId);

/// <summary>Сценарій «скасувати оренду».</summary>
public sealed class CancelRentalHandler(IUnitOfWork uow, IClock clock, IMapper mapper, IEventBus events)
{
    public async Task<RentalClosedDto> HandleAsync(CancelRentalCommand cmd, CancellationToken ct = default)
    {
        var rental = await uow.Rentals.GetByIdAsync(cmd.RentalId, ct) ?? throw new NotFoundException("Оренду");
        rental.Cancel(clock.Today);
        await uow.SaveChangesAsync(ct);

        await events.PublishAsync(new RentalCancelled(rental.Id, rental.CarId), ct);
        return mapper.Map<RentalClosedDto>(rental);
    }
}
