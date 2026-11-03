using CarRental.Application.Common.Persistence;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>Репозиторій орендарів: лише загальні операції, власних запитів поки не потрібно.</summary>
public interface IRenterRepository : IRepository<Renter>;
