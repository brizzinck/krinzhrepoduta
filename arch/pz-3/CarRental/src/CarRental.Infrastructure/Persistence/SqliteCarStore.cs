using CarRental.Application.Fleet;
using CarRental.Domain.Fleet;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence;

/// <summary>Реалізація ICarStore через EF Core та SQLite.</summary>
public sealed class SqliteCarStore(CarRentalDbContext db) : ICarStore
{
    public async Task<IReadOnlyList<Car>> List(CancellationToken ct) =>
        await db.Cars.AsNoTracking().OrderBy(c => c.Plate).ToListAsync(ct);

    public Task<Car?> Find(Guid id, CancellationToken ct) =>
        db.Cars.SingleOrDefaultAsync(c => c.Id == id, ct);

    public Task<bool> PlateExists(LicensePlate plate, CancellationToken ct) =>
        db.Cars.AnyAsync(c => c.Plate == plate, ct);

    public Task Add(Car car, CancellationToken ct)
    {
        db.Cars.Add(car);
        return Task.CompletedTask;
    }

    public Task Save(CancellationToken ct) => db.SaveOrConflict(ct);
}
