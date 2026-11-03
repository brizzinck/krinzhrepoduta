using CarRental.Application.Fleet;
using CarRental.Domain.Fleet;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence.Repositories;

public sealed class CarRepository(CarRentalDbContext db) : EfRepository<Car>(db), ICarRepository
{
    public override async Task<IReadOnlyList<Car>> GetAllAsync(CancellationToken ct) =>
        await Db.Cars.AsNoTracking().OrderBy(c => c.Plate).ToListAsync(ct);

    public Task<Car?> FindByPlateAsync(LicensePlate plate, CancellationToken ct) =>
        Db.Cars.SingleOrDefaultAsync(c => c.Plate == plate, ct);

    public Task<bool> PlateExistsAsync(LicensePlate plate, CancellationToken ct) =>
        Db.Cars.AnyAsync(c => c.Plate == plate, ct);

    public async Task<IReadOnlyList<Car>> ListAvailableAsync(CancellationToken ct) =>
        await Db.Cars.AsNoTracking().Where(c => c.Status == CarStatus.Available).OrderBy(c => c.Plate).ToListAsync(ct);
}
