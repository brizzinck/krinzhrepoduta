using System.Text.Json;
using CarRental.Application.Common.Caching;
using Enyim.Caching;
using Microsoft.Extensions.Logging;

namespace CarRental.Infrastructure.Caching;

/// <summary>Кеш на Memcached: значення зберігаються як JSON. Збій кешу лише записується в журнал.</summary>
public sealed class MemcachedCacheStore(IMemcachedClient client, ILogger<MemcachedCacheStore> logger) : ICacheStore
{
    public async Task<T?> GetAsync<T>(string key, CancellationToken ct) where T : class
    {
        try
        {
            var result = await client.GetAsync<string>(key);
            return result.Success && result.Value is not null ? JsonSerializer.Deserialize<T>(result.Value) : null;
        }
        catch (Exception ex)
        {
            logger.LogWarning(ex, "Кеш недоступний під час читання {Key}", key);
            return null;
        }
    }

    public async Task SetAsync<T>(string key, T value, TimeSpan ttl, CancellationToken ct) where T : class
    {
        try
        {
            if (!await client.SetAsync(key, JsonSerializer.Serialize(value), (int)ttl.TotalSeconds))
                logger.LogWarning("Кеш не зберіг значення {Key}", key);
        }
        catch (Exception ex)
        {
            logger.LogWarning(ex, "Кеш недоступний під час запису {Key}", key);
        }
    }

    public async Task RemoveAsync(string key, CancellationToken ct)
    {
        try
        {
            await client.RemoveAsync(key);
        }
        catch (Exception ex)
        {
            logger.LogWarning(ex, "Кеш недоступний під час видалення {Key}", key);
        }
    }
}
