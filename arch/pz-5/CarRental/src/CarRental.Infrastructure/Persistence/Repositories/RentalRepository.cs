using CarRental.Application.Rentals;
using CarRental.Domain.Rentals;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence.Repositories;

public sealed class RentalRepository(CarRentalDbContext db) : EfRepository<Rental>(db), IRentalRepository
{
    public override Task<IReadOnlyList<Rental>> GetAllAsync(CancellationToken ct) => ListByRenterAsync(null, ct);

    public async Task<IReadOnlyList<Rental>> ListByRenterAsync(Guid? renterId, CancellationToken ct)
    {
        var query = Db.Rentals.AsNoTracking();
        if (renterId is { } id)
            query = query.Where(r => r.RenterId == id);
        // Сортуємо в пам'яті: обсяг даних невеликий, а порядок за значенням VO не залежить від SQL.
        var all = await query.ToListAsync(ct);
        return all.OrderBy(r => r.Period.Start).ThenBy(r => r.Id).ToList();
    }

    public async Task<IReadOnlyList<RentalPeriod>> ActivePeriodsOfCarAsync(Guid carId, CancellationToken ct)
    {
        var active = await Db.Rentals.AsNoTracking()
            .Where(r => r.CarId == carId && r.Status == RentalStatus.Active)
            .ToListAsync(ct);
        return active.Select(r => r.Period).ToList();
    }
}
