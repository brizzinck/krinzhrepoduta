using CarRental.Domain.Fleet;

namespace CarRental.Application.Fleet;

/// <summary>Контракт збереження автомобілів. Належить Application, реалізація лежить в Infrastructure.</summary>
public interface ICarStore
{
    Task<IReadOnlyList<Car>> List(CancellationToken ct);
    Task<Car?> Find(Guid id, CancellationToken ct);
    Task<bool> PlateExists(LicensePlate plate, CancellationToken ct);
    // Готує додавання; фактичний запис виконує Save.
    Task Add(Car car, CancellationToken ct);
    Task Save(CancellationToken ct);
}
