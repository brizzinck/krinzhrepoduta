using AutoMapper;
using CarRental.Application.Common;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet.Dto;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using FluentValidation;

namespace CarRental.Application.Fleet;

/// <summary>Сценарії контексту Fleet: реєстрація авто, перегляд, обслуговування.</summary>
public sealed class CarService(IUnitOfWork uow, IMapper mapper, IValidator<RegisterCarCommand> validator)
{
    public async Task<IReadOnlyList<CarDto>> List(CancellationToken ct = default) =>
        mapper.Map<IReadOnlyList<CarDto>>(await uow.Cars.GetAllAsync(ct));

    public async Task<CarDto> Get(Guid id, CancellationToken ct = default) =>
        mapper.Map<CarDto>(await uow.Cars.GetByIdAsync(id, ct) ?? throw new NotFoundException("Автомобіль"));

    public async Task<CarDto> Register(RegisterCarCommand cmd, CancellationToken ct = default)
    {
        await validator.ValidateAndThrowAsync(cmd, ct);
        var carClass = Enum.Parse<CarClass>(cmd.Class!, ignoreCase: true);
        var car = Car.Register(new LicensePlate(cmd.Plate), cmd.Model, carClass, new Money(cmd.DailyRate), cmd.Mileage);
        await uow.Cars.AddAsync(car, ct);
        await uow.SaveChangesAsync(ct);
        return mapper.Map<CarDto>(car);
    }

    public async Task<CarDto> SendToMaintenance(Guid id, CancellationToken ct = default)
    {
        var car = await uow.Cars.GetByIdAsync(id, ct) ?? throw new NotFoundException("Автомобіль");
        car.SendToMaintenance();
        await uow.SaveChangesAsync(ct);
        return mapper.Map<CarDto>(car);
    }
}
