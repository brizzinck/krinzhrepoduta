using CarRental.Application.Fleet;
using CarRental.Application.Rentals;

namespace CarRental.Application.Common.Persistence;

/// <summary>
/// Одиниця роботи: координує кілька репозиторіїв і фіксує всі зміни бізнес-операції однією транзакцією.
/// </summary>
public interface IUnitOfWork
{
    ICarRepository Cars { get; }
    IRenterRepository Renters { get; }
    IRentalRepository Rentals { get; }

    /// <summary>Зберігає всі підготовлені зміни атомарно. Конфлікт версій повертається як ConcurrentChangeException.</summary>
    Task<int> SaveChangesAsync(CancellationToken ct);
}
