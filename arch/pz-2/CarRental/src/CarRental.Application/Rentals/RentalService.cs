using CarRental.Application.Common;
using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;
using CarRental.Domain.Common;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>
/// Сценарії оренди: відкрити, завершити, скасувати, переглянути.
/// Усі сценарії в одному класі, ціна рахується тут; реакції на зміни стану виконують підписники подій.
/// </summary>
public sealed class RentalService(
    IRentalStore rentals,
    IRenterStore renters,
    IFleetGateway fleet,
    IEventBus events)
{
    public async Task<RentalDto> Open(OpenRentalCommand cmd, CancellationToken ct = default)
    {
        var car = await fleet.GetRentableCar(cmd.CarId, ct) ?? throw new NotFoundException("Автомобіль");
        var renter = await renters.Find(cmd.RenterId, ct) ?? throw new NotFoundException("Орендаря");
        var period = new RentalPeriod(cmd.Start, cmd.End);
        var busy = await rentals.ActivePeriodsOfCar(car.Id, ct);

        var rental = Rental.Open(car, renter, period, CalculateTotal(car, period), Today(), busy);
        await rentals.Add(rental, ct);
        await rentals.Save(ct);

        // Публікуємо факт; хто і як на нього реагує, сервіс не знає.
        await events.PublishAsync(new RentalOpened(rental.Id, rental.CarId, rental.RenterId,
            rental.Period.Start, rental.Period.End, rental.Total.Amount), ct);
        return ToDto(rental);
    }

    public async Task<RentalDto> Complete(Guid id, int endMileage, CancellationToken ct = default)
    {
        var rental = await rentals.Find(id, ct) ?? throw new NotFoundException("Оренду");
        rental.Complete(endMileage);
        await rentals.Save(ct);

        await events.PublishAsync(new RentalCompleted(rental.Id, rental.CarId, endMileage), ct);
        return ToDto(rental);
    }

    public async Task<RentalDto> Cancel(Guid id, CancellationToken ct = default)
    {
        var rental = await rentals.Find(id, ct) ?? throw new NotFoundException("Оренду");
        rental.Cancel(Today());
        await rentals.Save(ct);
        await events.PublishAsync(new RentalCancelled(rental.Id, rental.CarId), ct);
        return ToDto(rental);
    }

    public async Task<RentalDto> Get(Guid id, CancellationToken ct = default) =>
        ToDto(await rentals.Find(id, ct) ?? throw new NotFoundException("Оренду"));

    public async Task<IReadOnlyList<RentalDto>> List(Guid? renterId, CancellationToken ct = default) =>
        (await rentals.List(renterId, ct)).Select(ToDto).ToArray();

    /// <summary>Тариф: коефіцієнт за класом авто та знижка 10% за оренду від 7 днів.</summary>
    private static Money CalculateTotal(RentableCar car, RentalPeriod period)
    {
        var multiplier = car.Class switch
        {
            CarClass.Economy => 1.0m,
            CarClass.Comfort => 1.25m,
            CarClass.Premium => 1.6m,
            _ => throw new DomainRuleException("Невідомий клас автомобіля.")
        };
        var total = car.DailyRate.Multiply(multiplier * period.Days);
        if (period.Days >= 7)
            total = total.Multiply(0.9m);
        return total;
    }

    private static DateOnly Today() => DateOnly.FromDateTime(DateTime.UtcNow);

    private static RentalDto ToDto(Rental r) => new(r.Id, r.CarId, r.RenterId, r.Period.Start, r.Period.End,
        r.Period.Days, r.DailyRate.Amount, r.Total.Amount, r.Status.ToString(), r.StartMileage, r.EndMileage);
}
