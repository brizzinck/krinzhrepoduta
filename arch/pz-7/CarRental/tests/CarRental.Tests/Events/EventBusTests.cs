using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;
using CarRental.Infrastructure.Events;
using Microsoft.Extensions.DependencyInjection;

namespace CarRental.Tests.Events;

public class EventBusTests
{
    private sealed record Ping(int N);
    private sealed record Pong(int N);

    private sealed class Recorder<T>(string name, List<string> log) : IEventHandler<T>
    {
        public Task HandleAsync(T evt, CancellationToken ct)
        {
            log.Add($"{name}:{evt}");
            return Task.CompletedTask;
        }
    }

    /// <summary>Шина зі справжнім контейнером: підписники додаються реєстрацією, а не викликом Subscribe.</summary>
    private static IEventBus BusWith(Action<IServiceCollection> register)
    {
        var services = new ServiceCollection();
        services.AddSingleton<IEventBus, InProcessEventBus>();
        register(services);
        return services.BuildServiceProvider(new ServiceProviderOptions { ValidateScopes = true, ValidateOnBuild = true })
            .GetRequiredService<IEventBus>();
    }

    [Fact]
    public async Task Publish_calls_every_subscriber_in_registration_order()
    {
        var log = new List<string>();
        var bus = BusWith(s =>
        {
            s.AddScoped<IEventHandler<Ping>>(_ => new Recorder<Ping>("first", log));
            s.AddScoped<IEventHandler<Ping>>(_ => new Recorder<Ping>("second", log));
        });
        await bus.PublishAsync(new Ping(1), default);
        Assert.Equal(["first:Ping { N = 1 }", "second:Ping { N = 1 }"], log);
    }

    [Fact]
    public async Task Handlers_of_other_events_are_not_called()
    {
        var log = new List<string>();
        var bus = BusWith(s => s.AddScoped<IEventHandler<Pong>>(_ => new Recorder<Pong>("pong", log)));
        await bus.PublishAsync(new Ping(1), default);
        Assert.Empty(log);
    }

    [Fact]
    public async Task Event_without_subscribers_is_not_an_error() =>
        await BusWith(_ => { }).PublishAsync(new Ping(1), default);

    [Fact]
    public async Task Singleton_bus_runs_scoped_handlers_in_a_fresh_scope_per_publish()
    {
        var instances = new List<Guid>();
        var bus = BusWith(s => s.AddScoped<IEventHandler<Ping>>(_ => new ScopeProbe(Guid.NewGuid(), instances)));
        await bus.PublishAsync(new Ping(1), default);
        await bus.PublishAsync(new Ping(2), default);
        Assert.Equal(2, instances.Distinct().Count()); // обробник створюється заново на кожну публікацію
    }

    private sealed class ScopeProbe(Guid id, List<Guid> seen) : IEventHandler<Ping>
    {
        public Task HandleAsync(Ping evt, CancellationToken ct)
        {
            seen.Add(id);
            return Task.CompletedTask;
        }
    }

    [Fact]
    public void Events_are_immutable_facts_with_minimal_data()
    {
        var evt = new RentalCancelled(Guid.NewGuid(), Guid.NewGuid());
        Assert.Equal(evt, evt with { });
        Assert.Equal(2, typeof(RentalCancelled).GetProperties().Length);
        Assert.True(typeof(RentalOpened).IsSealed);
    }
}
