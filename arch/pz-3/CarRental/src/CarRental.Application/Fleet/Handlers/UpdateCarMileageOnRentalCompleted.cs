using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Fleet.Handlers;

/// <summary>
/// Контекст Fleet реагує на подію контексту Rentals: оновлює пробіг автомобіля.
/// Rentals не знає, що хтось стежить за його подіями.
/// </summary>
public sealed class UpdateCarMileageOnRentalCompleted(ICarStore cars) : IEventHandler<RentalCompleted>
{
    public async Task HandleAsync(RentalCompleted evt, CancellationToken ct)
    {
        var car = await cars.Find(evt.CarId, ct);
        // Пробіг автомобіля лише зростає: застаріле значення ігноруємо.
        if (car is null || evt.EndMileage <= car.Mileage)
            return;
        car.RecordMileage(evt.EndMileage);
        await cars.Save(ct);
    }
}
