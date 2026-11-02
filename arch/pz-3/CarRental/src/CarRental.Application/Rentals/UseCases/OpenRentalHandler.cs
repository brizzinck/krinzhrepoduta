using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.Pricing;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals.UseCases;

/// <summary>Сценарій «відкрити оренду»: завантажити, порахувати ціну, виконати доменну операцію, зберегти, опублікувати подію.</summary>
public sealed class OpenRentalHandler(
    IRentalStore rentals,
    IRenterStore renters,
    IFleetGateway fleet,
    IPricingService pricing,
    IEventBus events)
{
    public async Task<RentalDto> HandleAsync(OpenRentalCommand cmd, CancellationToken ct = default)
    {
        var car = await fleet.GetRentableCar(cmd.CarId, ct) ?? throw new NotFoundException("Автомобіль");
        var renter = await renters.Find(cmd.RenterId, ct) ?? throw new NotFoundException("Орендаря");
        var period = new RentalPeriod(cmd.Start, cmd.End);
        var busy = await rentals.ActivePeriodsOfCar(car.Id, ct);

        var today = DateOnly.FromDateTime(DateTime.UtcNow);
        var rental = Rental.Open(car, renter, period, pricing.Calculate(car, period), today, busy);
        await rentals.Add(rental, ct);
        await rentals.Save(ct);

        await events.PublishAsync(new RentalOpened(rental.Id, rental.CarId, rental.RenterId,
            rental.Period.Start, rental.Period.End, rental.Total.Amount), ct);
        return RentalMapper.ToDto(rental);
    }
}
