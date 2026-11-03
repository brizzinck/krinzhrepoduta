using AutoMapper;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Dto;
using CarRental.Domain.Rentals;

namespace CarRental.Infrastructure.Integration;

/// <summary>Адаптер між контекстами: перетворює Car з Fleet на RentableCar для Rentals.</summary>
public sealed class FleetGateway(ICarRepository cars, IMapper mapper) : IFleetGateway
{
    public async Task<RentableCar?> GetRentableCar(Guid carId, CancellationToken ct)
    {
        var car = await cars.GetByIdAsync(carId, ct);
        return car is null ? null : new RentableCar(car.Id, car.DailyRate, car.Class, car.Mileage, car.IsRentable);
    }

    public async Task<CarSummaryDto?> GetCarSummary(Guid carId, CancellationToken ct)
    {
        var car = await cars.GetByIdAsync(carId, ct);
        return car is null ? null : mapper.Map<CarSummaryDto>(car);
    }
}
