using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;

namespace CarRental.Tests.Fakes;

/// <summary>Репозиторій у пам'яті: показує, що Application працює з абстракціями, а не з EF Core.</summary>
public class InMemoryRepository<T> : IRepository<T> where T : Entity
{
    protected Dictionary<Guid, T> Items { get; } = [];

    public Task<T?> GetByIdAsync(Guid id, CancellationToken ct) => Task.FromResult(Items.GetValueOrDefault(id));
    public Task<IReadOnlyList<T>> GetAllAsync(CancellationToken ct) => Task.FromResult<IReadOnlyList<T>>(Items.Values.ToList());
    public Task AddAsync(T entity, CancellationToken ct)
    {
        Items[entity.Id] = entity;
        return Task.CompletedTask;
    }
    public void Update(T entity) => Items[entity.Id] = entity;
    public void Delete(T entity) => Items.Remove(entity.Id);
}

public sealed class InMemoryCarRepository : InMemoryRepository<Car>, ICarRepository
{
    public Task<Car?> FindByPlateAsync(LicensePlate plate, CancellationToken ct) =>
        Task.FromResult(Items.Values.SingleOrDefault(c => c.Plate == plate));
    public Task<bool> PlateExistsAsync(LicensePlate plate, CancellationToken ct) =>
        Task.FromResult(Items.Values.Any(c => c.Plate == plate));
    public Task<IReadOnlyList<Car>> ListAvailableAsync(CancellationToken ct) =>
        Task.FromResult<IReadOnlyList<Car>>(Items.Values.Where(c => c.IsRentable).ToList());
}

public sealed class InMemoryRenterRepository : InMemoryRepository<Renter>, IRenterRepository;

public sealed class InMemoryRentalRepository : InMemoryRepository<Rental>, IRentalRepository
{
    public Task<IReadOnlyList<Rental>> ListByRenterAsync(Guid? renterId, CancellationToken ct) =>
        Task.FromResult<IReadOnlyList<Rental>>(Items.Values.Where(r => renterId is null || r.RenterId == renterId).ToList());
    public Task<IReadOnlyList<RentalPeriod>> ActivePeriodsOfCarAsync(Guid carId, CancellationToken ct) =>
        Task.FromResult<IReadOnlyList<RentalPeriod>>(Items.Values
            .Where(r => r.CarId == carId && r.Status == RentalStatus.Active).Select(r => r.Period).ToList());
}

/// <summary>Unit of Work у пам'яті: лічить фіксації, щоб тести бачили, скільки разів операція «зберігала».</summary>
public sealed class InMemoryUnitOfWork : IUnitOfWork
{
    public InMemoryCarRepository CarRepository { get; } = new();
    public InMemoryRenterRepository RenterRepository { get; } = new();
    public InMemoryRentalRepository RentalRepository { get; } = new();
    public int SaveCount { get; private set; }

    public ICarRepository Cars => CarRepository;
    public IRenterRepository Renters => RenterRepository;
    public IRentalRepository Rentals => RentalRepository;

    public Task<int> SaveChangesAsync(CancellationToken ct)
    {
        SaveCount++;
        return Task.FromResult(0);
    }
}
