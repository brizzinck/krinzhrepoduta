using CarRental.Application.Common.Persistence;
using CarRental.Domain.Common;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence.Repositories;

/// <summary>
/// Загальна реалізація репозиторію на EF Core. Бізнес-логіки немає: лише доступ до колекції агрегатів.
/// Зміни не фіксуються тут: це робить Unit of Work.
/// </summary>
public class EfRepository<T>(CarRentalDbContext db) : IRepository<T> where T : Entity
{
    protected CarRentalDbContext Db { get; } = db;

    public virtual Task<T?> GetByIdAsync(Guid id, CancellationToken ct) =>
        Db.Set<T>().SingleOrDefaultAsync(e => e.Id == id, ct);

    /// <summary>Список для читання: без відстеження змін.</summary>
    public virtual async Task<IReadOnlyList<T>> GetAllAsync(CancellationToken ct) =>
        await Db.Set<T>().AsNoTracking().ToListAsync(ct);

    public async Task AddAsync(T entity, CancellationToken ct) => await Db.Set<T>().AddAsync(entity, ct);

    public void Update(T entity) => Db.Set<T>().Update(entity);

    public void Delete(T entity) => Db.Set<T>().Remove(entity);
}
