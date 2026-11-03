using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Fleet.Handlers;

/// <summary>
/// Контекст Fleet реагує на подію контексту Rentals: оновлює пробіг автомобіля.
/// Rentals не знає, що хтось стежить за його подіями. Це окрема одиниця роботи: зміни Fleet фіксуються своїм SaveChanges.
/// </summary>
public sealed class UpdateCarMileageOnRentalCompleted(IUnitOfWork uow) : IEventHandler<RentalCompleted>
{
    public async Task HandleAsync(RentalCompleted evt, CancellationToken ct)
    {
        var car = await uow.Cars.GetByIdAsync(evt.CarId, ct);
        // Пробіг автомобіля лише зростає: застаріле значення ігноруємо.
        if (car is null || evt.EndMileage <= car.Mileage)
            return;
        car.RecordMileage(evt.EndMileage);
        await uow.SaveChangesAsync(ct);
    }
}
