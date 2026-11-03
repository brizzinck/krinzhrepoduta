using AutoMapper;
using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Rentals.Dto;
using CarRental.Application.Rentals.Events;
using FluentValidation;

namespace CarRental.Application.Rentals.UseCases;

public sealed record CompleteRentalCommand(Guid RentalId, int EndMileage);

/// <summary>Сценарій «завершити оренду».</summary>
public sealed class CompleteRentalHandler(IUnitOfWork uow, IMapper mapper, IEventBus events, IValidator<CompleteRentalCommand> validator)
{
    public async Task<RentalClosedDto> HandleAsync(CompleteRentalCommand cmd, CancellationToken ct = default)
    {
        await validator.ValidateAndThrowAsync(cmd, ct);
        var rental = await uow.Rentals.GetByIdAsync(cmd.RentalId, ct) ?? throw new NotFoundException("Оренду");
        rental.Complete(cmd.EndMileage);
        await uow.SaveChangesAsync(ct);

        await events.PublishAsync(new RentalCompleted(rental.Id, rental.CarId, cmd.EndMileage), ct);
        return mapper.Map<RentalClosedDto>(rental);
    }
}
