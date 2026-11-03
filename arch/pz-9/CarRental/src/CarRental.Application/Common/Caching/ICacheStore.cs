namespace CarRental.Application.Common.Caching;

/// <summary>Порт кешу. Недоступність кешу не є помилкою: реалізація повертає промах, а застосунок іде в БД.</summary>
public interface ICacheStore
{
    Task<T?> GetAsync<T>(string key, CancellationToken ct) where T : class;
    Task SetAsync<T>(string key, T value, TimeSpan ttl, CancellationToken ct) where T : class;
    Task RemoveAsync(string key, CancellationToken ct);
}
