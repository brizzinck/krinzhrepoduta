using CarRental.Application.Common;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence;

/// <summary>
/// Одиниця роботи над одним DbContext: репозиторії діляться ним, а SaveChangesAsync фіксує всі зміни
/// однією транзакцією. Живе як Scoped: один запит, одна одиниця роботи. Єдине місце, де виняток EF
/// про конфлікт версій перетворюється на тип Application.
/// </summary>
public sealed class UnitOfWork(
    CarRentalDbContext db,
    ICarRepository cars,
    IRenterRepository renters,
    IRentalRepository rentals) : IUnitOfWork
{
    public ICarRepository Cars { get; } = cars;
    public IRenterRepository Renters { get; } = renters;
    public IRentalRepository Rentals { get; } = rentals;

    public async Task<int> SaveChangesAsync(CancellationToken ct)
    {
        try { return await db.SaveChangesAsync(ct); }
        catch (DbUpdateConcurrencyException) { throw new ConcurrentChangeException(); }
    }
}
