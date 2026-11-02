using CarRental.Application.Rentals;
using CarRental.Domain.Rentals;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence;

public sealed class SqliteRenterStore(CarRentalDbContext db) : IRenterStore
{
    public Task<Renter?> Find(Guid id, CancellationToken ct) =>
        db.Renters.SingleOrDefaultAsync(r => r.Id == id, ct);

    public Task Add(Renter renter, CancellationToken ct)
    {
        db.Renters.Add(renter);
        return Task.CompletedTask;
    }

    public Task Save(CancellationToken ct) => db.SaveOrConflict(ct);
}
