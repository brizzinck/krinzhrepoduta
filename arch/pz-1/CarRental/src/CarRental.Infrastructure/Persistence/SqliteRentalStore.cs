using CarRental.Application.Rentals;
using CarRental.Domain.Rentals;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence;

public sealed class SqliteRentalStore(CarRentalDbContext db) : IRentalStore
{
    public Task<Rental?> Find(Guid id, CancellationToken ct) =>
        db.Rentals.SingleOrDefaultAsync(r => r.Id == id, ct);

    public async Task<IReadOnlyList<Rental>> List(Guid? renterId, CancellationToken ct)
    {
        var query = db.Rentals.AsNoTracking();
        if (renterId is { } id)
            query = query.Where(r => r.RenterId == id);
        // Сортуємо в пам'яті: обсяг даних невеликий, а порядок за значенням VO не залежить від SQL.
        var all = await query.ToListAsync(ct);
        return all.OrderBy(r => r.Period.Start).ThenBy(r => r.Id).ToList();
    }

    public async Task<IReadOnlyList<RentalPeriod>> ActivePeriodsOfCar(Guid carId, CancellationToken ct)
    {
        var active = await db.Rentals.AsNoTracking()
            .Where(r => r.CarId == carId && r.Status == RentalStatus.Active)
            .ToListAsync(ct);
        return active.Select(r => r.Period).ToList();
    }

    public Task Add(Rental rental, CancellationToken ct)
    {
        db.Rentals.Add(rental);
        return Task.CompletedTask;
    }

    public Task Save(CancellationToken ct) => db.SaveOrConflict(ct);
}
