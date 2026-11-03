using CarRental.Application.Common.Persistence;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>Читання орендарів для клієнтів, яким записувати нічого не потрібно.</summary>
public interface IRenterReader : IReadRepository<Renter>;

/// <summary>Репозиторій орендарів: лише загальні операції, власних запитів поки не потрібно.</summary>
public interface IRenterRepository : IRepository<Renter>, IRenterReader;
