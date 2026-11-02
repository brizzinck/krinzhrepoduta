using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>Читання оренд: для клієнтів, яким записувати нічого не потрібно.</summary>
public interface IRentalReader
{
    Task<Rental?> Find(Guid id, CancellationToken ct);
    Task<IReadOnlyList<Rental>> List(Guid? renterId, CancellationToken ct);
}

/// <summary>Читання й запис оренд для сценаріїв, що змінюють стан.</summary>
public interface IRentalStore : IRentalReader
{
    /// <summary>Періоди активних оренд автомобіля: потрібні, щоб Domain перевірив перетин.</summary>
    Task<IReadOnlyList<RentalPeriod>> ActivePeriodsOfCar(Guid carId, CancellationToken ct);
    Task Add(Rental rental, CancellationToken ct);
    Task Save(CancellationToken ct);
}
