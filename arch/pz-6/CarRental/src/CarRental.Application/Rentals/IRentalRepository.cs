using CarRental.Application.Common.Persistence;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>Читання оренд (запити предметної області) для клієнтів, яким записувати нічого не потрібно.</summary>
public interface IRentalReader : IReadRepository<Rental>
{
    /// <summary>Оренди за початком; з фільтром за орендарем, якщо він указаний.</summary>
    Task<IReadOnlyList<Rental>> ListByRenterAsync(Guid? renterId, CancellationToken ct);
}

/// <summary>Репозиторій оренд: читання й запис плюс запити, потрібні сценаріям.</summary>
public interface IRentalRepository : IRepository<Rental>, IRentalReader
{
    /// <summary>Періоди активних оренд автомобіля: потрібні, щоб Domain перевірив перетин.</summary>
    Task<IReadOnlyList<RentalPeriod>> ActivePeriodsOfCarAsync(Guid carId, CancellationToken ct);
}
