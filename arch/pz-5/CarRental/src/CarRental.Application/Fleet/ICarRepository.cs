using CarRental.Application.Common.Persistence;
using CarRental.Domain.Fleet;

namespace CarRental.Application.Fleet;

/// <summary>Репозиторій автомобілів: загальні операції та запити предметної області Fleet.</summary>
public interface ICarRepository : IRepository<Car>
{
    Task<Car?> FindByPlateAsync(LicensePlate plate, CancellationToken ct);
    Task<bool> PlateExistsAsync(LicensePlate plate, CancellationToken ct);

    /// <summary>Автомобілі, які зараз можна здавати в оренду (за станом).</summary>
    Task<IReadOnlyList<Car>> ListAvailableAsync(CancellationToken ct);
}
