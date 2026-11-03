using CarRental.Application.Common.Caching;
using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.UseCases;

namespace CarRental.Application.Rentals.Handlers;

/// <summary>Після зміни оренди видаляє її з кешу. Подія публікується після фіксації, тому БД уже містить нові дані.</summary>
public sealed class InvalidateRentalCache(ICacheStore cache) : IEventHandler<RentalCompleted>, IEventHandler<RentalCancelled>
{
    public Task HandleAsync(RentalCompleted evt, CancellationToken ct) =>
        cache.RemoveAsync(CachedRentalQueryService.Key(evt.RentalId), ct);

    public Task HandleAsync(RentalCancelled evt, CancellationToken ct) =>
        cache.RemoveAsync(CachedRentalQueryService.Key(evt.RentalId), ct);
}
