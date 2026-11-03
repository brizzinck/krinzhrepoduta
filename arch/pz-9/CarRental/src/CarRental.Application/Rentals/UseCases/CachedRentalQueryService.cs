using CarRental.Application.Common.Caching;
using CarRental.Application.Rentals.Dto;
using Microsoft.Extensions.Logging;

namespace CarRental.Application.Rentals.UseCases;

/// <summary>
/// Декоратор: кешує деталі оренди за схемою Cache-Aside. Бізнес-логіки тут немає, а сервіс читання про кеш не знає.
/// Список не кешується: його важко інвалідувати, бо він залежить від багатьох оренд.
/// </summary>
public sealed class CachedRentalQueryService(IRentalQueryService inner, ICacheStore cache,
    ILogger<CachedRentalQueryService> logger) : IRentalQueryService
{
    public static readonly TimeSpan Ttl = TimeSpan.FromSeconds(60);

    public static string Key(Guid id) => $"rental:{id}";

    public async Task<RentalDetailsDto> Get(Guid id, CancellationToken ct = default)
    {
        var cached = await cache.GetAsync<RentalDetailsDto>(Key(id), ct);
        if (cached is not null)
        {
            logger.LogInformation("Кеш HIT {Key}", Key(id));
            return cached;
        }
        logger.LogInformation("Кеш MISS {Key}", Key(id));
        var rental = await inner.Get(id, ct);
        await cache.SetAsync(Key(id), rental, Ttl, ct);
        return rental;
    }

    public Task<IReadOnlyList<RentalListItemDto>> List(Guid? renterId, CancellationToken ct = default) =>
        inner.List(renterId, ct);
}
