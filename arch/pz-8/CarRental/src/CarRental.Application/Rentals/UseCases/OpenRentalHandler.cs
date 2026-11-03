using AutoMapper;
using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Rentals.Dto;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.Pricing;
using CarRental.Domain.Rentals;
using FluentValidation;

namespace CarRental.Application.Rentals.UseCases;

/// <summary>
/// Сценарій «відкрити оренду»: прочитати авто й орендаря, порахувати ціну, виконати доменну операцію,
/// додати оренду й зберегти все однією фіксацією Unit of Work, після чого опублікувати подію.
/// </summary>
public sealed class OpenRentalHandler(
    IUnitOfWork uow,
    IFleetGateway fleet,
    IPricingService pricing,
    IClock clock,
    IMapper mapper,
    IEventBus events,
    IValidator<OpenRentalCommand> validator)
{
    public async Task<RentalCreatedDto> HandleAsync(OpenRentalCommand cmd, CancellationToken ct = default)
    {
        await validator.ValidateAndThrowAsync(cmd, ct);
        var car = await fleet.GetRentableCar(cmd.CarId, ct) ?? throw new NotFoundException("Автомобіль");
        var renter = await uow.Renters.GetByIdAsync(cmd.RenterId, ct) ?? throw new NotFoundException("Орендаря");
        var period = new RentalPeriod(cmd.Start, cmd.End);
        var busy = await uow.Rentals.ActivePeriodsOfCarAsync(car.Id, ct);

        var rental = Rental.Open(car, renter, period, pricing.Calculate(car, period), clock.Today, busy);
        await uow.Rentals.AddAsync(rental, ct);
        await uow.SaveChangesAsync(ct);

        await events.PublishAsync(new RentalOpened(rental.Id, rental.CarId, rental.RenterId,
            rental.Period.Start, rental.Period.End, rental.Total.Amount), ct);
        return mapper.Map<RentalCreatedDto>(rental);
    }
}
