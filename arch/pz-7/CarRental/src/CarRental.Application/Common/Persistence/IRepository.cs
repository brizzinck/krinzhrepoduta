using CarRental.Domain.Common;

namespace CarRental.Application.Common.Persistence;

/// <summary>Читання агрегатів: клієнтам, яким записувати нічого не потрібно (ISP).</summary>
public interface IReadRepository<T> where T : Entity
{
    Task<T?> GetByIdAsync(Guid id, CancellationToken ct);
    Task<IReadOnlyList<T>> GetAllAsync(CancellationToken ct);
}

/// <summary>
/// Колекція агрегатів певного типу. Інтерфейс належить Application, реалізація (EF Core) лежить в Infrastructure.
/// Репозиторій лише готує зміни; фіксує їх Unit of Work (IUnitOfWork.SaveChangesAsync).
/// </summary>
public interface IRepository<T> : IReadRepository<T> where T : Entity
{
    Task AddAsync(T entity, CancellationToken ct);

    /// <summary>Приєднує об'єкт, відключений від відстеження. Викликати до зміни об'єкта: версія на цей момент стає очікуваною.</summary>
    void Update(T entity);

    void Delete(T entity);
}
