using CarRental.Application.Common;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;

namespace CarRental.Application.Fleet;

/// <summary>Сценарії контексту Fleet: реєстрація авто, перегляд, обслуговування.</summary>
public sealed class CarService(ICarStore store)
{
    public async Task<IReadOnlyList<CarDto>> List(CancellationToken ct = default) =>
        (await store.List(ct)).Select(ToDto).ToArray();

    public async Task<CarDto> Get(Guid id, CancellationToken ct = default) =>
        ToDto(await store.Find(id, ct) ?? throw new NotFoundException("Автомобіль"));

    public async Task<CarDto> Register(string? plate, string? model, CarClass carClass, decimal dailyRate, int mileage,
        CancellationToken ct = default)
    {
        var licensePlate = new LicensePlate(plate);
        if (await store.PlateExists(licensePlate, ct))
            throw new DomainConflictException("Автомобіль із таким номером уже зареєстрований.");
        var car = Car.Register(licensePlate, model, carClass, new Money(dailyRate), mileage);
        await store.Add(car, ct);
        await store.Save(ct);
        return ToDto(car);
    }

    public async Task<CarDto> SendToMaintenance(Guid id, CancellationToken ct = default)
    {
        var car = await store.Find(id, ct) ?? throw new NotFoundException("Автомобіль");
        car.SendToMaintenance();
        await store.Save(ct);
        return ToDto(car);
    }

    private static CarDto ToDto(Car car) => new(car.Id, car.Plate.Value, car.Model, car.Class.ToString(),
        car.DailyRate.Amount, car.Mileage, car.Status.ToString());
}
