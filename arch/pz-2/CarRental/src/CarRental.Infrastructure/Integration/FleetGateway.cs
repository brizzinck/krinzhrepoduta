using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Domain.Rentals;

namespace CarRental.Infrastructure.Integration;

/// <summary>Адаптер між контекстами: перетворює Car з Fleet на RentableCar для Rentals.</summary>
public sealed class FleetGateway(ICarStore cars) : IFleetGateway
{
    public async Task<RentableCar?> GetRentableCar(Guid carId, CancellationToken ct)
    {
        var car = await cars.Find(carId, ct);
        return car is null ? null : new RentableCar(car.Id, car.DailyRate, car.Class, car.Mileage, car.IsRentable);
    }
}
