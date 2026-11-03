using CarRental.Application.Common.Caching;
using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.UseCases;
using CarRental.Domain.Common;
using CarRental.Infrastructure.Caching;
using CarRental.Tests.Integration;
using Enyim.Caching;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging.Abstractions;

namespace CarRental.Tests.Caching;

public class CachingTests
{
    private static readonly RentalDetailsDto Sample = new(Guid.NewGuid(),
        new CarSummaryDto(Guid.NewGuid(), "AA0001BB", "Skoda Fabia"),
        new RenterSummaryDto(Guid.NewGuid(), "Іваненко Іван", "ivan@example.com"),
        new PeriodDto(new DateOnly(2030, 1, 1), new DateOnly(2030, 1, 4), 3),
        new MoneyDto(800m, "UAH", "800.00 UAH"), new MoneyDto(2400m, "UAH", "2400.00 UAH"), "Active", 1000, null);

    private sealed class CountingQueries(RentalDetailsDto dto) : IRentalQueryService
    {
        public int Calls { get; private set; }
        public Task<RentalDetailsDto> Get(Guid id, CancellationToken ct = default)
        {
            Calls++;
            return Task.FromResult(dto);
        }
        public Task<IReadOnlyList<RentalListItemDto>> List(Guid? renterId, CancellationToken ct = default)
        {
            Calls++;
            return Task.FromResult<IReadOnlyList<RentalListItemDto>>([]);
        }
    }

    private static CachedRentalQueryService Decorator(IRentalQueryService inner, ICacheStore cache) =>
        new(inner, cache, NullLogger<CachedRentalQueryService>.Instance);

    [Fact]
    public async Task First_read_goes_to_the_source_and_second_is_served_from_cache_with_ttl()
    {
        var cache = new InMemoryCacheStore();
        var inner = new CountingQueries(Sample);
        var service = Decorator(inner, cache);

        Assert.Equal(Sample, await service.Get(Sample.Id));
        Assert.Equal(Sample, await service.Get(Sample.Id));

        Assert.Equal(1, inner.Calls);
        Assert.Equal([$"rental:{Sample.Id}"], cache.Keys);
        Assert.Equal(TimeSpan.FromSeconds(60), cache.LastTtl);
    }

    [Fact]
    public async Task List_is_not_cached()
    {
        var inner = new CountingQueries(Sample);
        var service = Decorator(inner, new InMemoryCacheStore());
        await service.List(null);
        await service.List(null);
        Assert.Equal(2, inner.Calls);
    }

    private sealed class AlwaysMissCache : ICacheStore
    {
        public Task<T?> GetAsync<T>(string key, CancellationToken ct) where T : class => Task.FromResult<T?>(null);
        public Task SetAsync<T>(string key, T value, TimeSpan ttl, CancellationToken ct) where T : class => Task.CompletedTask;
        public Task RemoveAsync(string key, CancellationToken ct) => Task.CompletedTask;
    }

    [Fact]
    public async Task Cache_that_always_misses_still_returns_data_from_the_source()
    {
        var inner = new CountingQueries(Sample);
        var service = Decorator(inner, new AlwaysMissCache());
        Assert.Equal(Sample, await service.Get(Sample.Id));
        Assert.Equal(Sample, await service.Get(Sample.Id));
        Assert.Equal(2, inner.Calls);
    }

    [Fact]
    public void Cached_value_survives_json_round_trip()
    {
        var json = System.Text.Json.JsonSerializer.Serialize(Sample);
        Assert.Equal(Sample, System.Text.Json.JsonSerializer.Deserialize<RentalDetailsDto>(json));
    }

    private static async Task<(Guid Car, Guid Renter)> SeedAsync(SqliteFixture f)
    {
        await using var db = f.NewContext();
        var car = await f.Cars(db).Register("AA1234BB", "Skoda Fabia", CarClass.Economy, 800m, 10_000);
        var renter = await f.Renters(db).Register("Іваненко Іван Іванович", "ivan@example.com",
            new DateOnly(1990, 5, 20), "ABC123456", f.Clock.Today.AddYears(5));
        return (car.Id, renter.Id);
    }

    [Fact]
    public async Task Controllers_get_the_caching_decorator_from_the_container()
    {
        using var f = new SqliteFixture();
        await using var request = f.NewContext();
        Assert.IsType<CachedRentalQueryService>(request.Get<IRentalQueryService>());
    }

    [Theory]
    [InlineData(true)]
    [InlineData(false)]
    public async Task Completing_or_cancelling_removes_the_rental_from_cache_and_next_read_is_fresh(bool complete)
    {
        using var f = new SqliteFixture();
        var (car, renter) = await SeedAsync(f);
        var today = f.Clock.Today;
        Guid id;
        await using (var db = f.NewContext())
            id = (await f.Rentals(db).Open(new OpenRentalCommand(car, renter, today.AddDays(1), today.AddDays(4)))).Id;
        await using (var db = f.NewContext())
            Assert.Equal("Active", (await db.Get<IRentalQueryService>().Get(id)).Status);
        Assert.Contains($"rental:{id}", f.Cache.Keys);

        await using (var db = f.NewContext())
        {
            if (complete)
                await f.Rentals(db).Complete(id, 10_480);
            else
                await f.Rentals(db).Cancel(id);
        }
        Assert.DoesNotContain($"rental:{id}", f.Cache.Keys);

        await using var read = f.NewContext();
        Assert.Equal(complete ? "Completed" : "Cancelled", (await read.Get<IRentalQueryService>().Get(id)).Status);
    }

    private sealed class FailingHandler : IEventHandler<RentalCompleted>
    {
        public Task HandleAsync(RentalCompleted evt, CancellationToken ct) =>
            throw new InvalidOperationException("Збій обробника");
    }

    [Fact]
    public async Task Failure_of_a_later_handler_does_not_leave_stale_data_in_cache()
    {
        using var f = new SqliteFixture(services =>
            services.AddScoped<IEventHandler<RentalCompleted>, FailingHandler>());
        var (car, renter) = await SeedAsync(f);
        var today = f.Clock.Today;
        Guid id;
        await using (var db = f.NewContext())
            id = (await f.Rentals(db).Open(new OpenRentalCommand(car, renter, today.AddDays(1), today.AddDays(4)))).Id;
        await using (var db = f.NewContext())
            await db.Get<IRentalQueryService>().Get(id);

        await using (var db = f.NewContext())
            await Assert.ThrowsAsync<InvalidOperationException>(() => f.Rentals(db).Complete(id, 10_480));

        Assert.DoesNotContain($"rental:{id}", f.Cache.Keys);
    }

    [Fact]
    public async Task Memcached_store_returns_a_miss_when_the_server_is_unreachable()
    {
        // Справжній клієнт Enyim на порт, де нікого немає: читання, запис і видалення не кидають винятків.
        var services = new ServiceCollection();
        services.AddLogging();
        services.AddEnyimMemcached(o => o.AddServer("127.0.0.1", 1));
        await using var provider = services.BuildServiceProvider();
        var store = new MemcachedCacheStore(provider.GetRequiredService<IMemcachedClient>(), NullLogger<MemcachedCacheStore>.Instance);

        Assert.Null(await store.GetAsync<RentalDetailsDto>("rental:x", default));
        await store.SetAsync("rental:x", Sample, TimeSpan.FromSeconds(60), default);
        await store.RemoveAsync("rental:x", default);
    }
}
