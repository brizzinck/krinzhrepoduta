using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

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

    [Fact]
    public async Task Publish_calls_every_subscriber_in_subscription_order()
    {
        var log = new List<string>();
        var bus = new InProcessEventBus();
        bus.Subscribe(new Recorder<Ping>("first", log));
        bus.Subscribe(new Recorder<Ping>("second", log));
        await bus.PublishAsync(new Ping(1), default);
        Assert.Equal(["first:Ping { N = 1 }", "second:Ping { N = 1 }"], log);
    }

    [Fact]
    public async Task Handlers_of_other_events_are_not_called()
    {
        var log = new List<string>();
        var bus = new InProcessEventBus();
        bus.Subscribe(new Recorder<Pong>("pong", log));
        await bus.PublishAsync(new Ping(1), default);
        Assert.Empty(log);
    }

    [Fact]
    public async Task Event_without_subscribers_is_not_an_error() =>
        await new InProcessEventBus().PublishAsync(new Ping(1), default);

    [Fact]
    public void Events_are_immutable_facts_with_minimal_data()
    {
        var evt = new RentalCancelled(Guid.NewGuid(), Guid.NewGuid());
        Assert.Equal(evt, evt with { });
        Assert.Equal(2, typeof(RentalCancelled).GetProperties().Length);
        Assert.True(typeof(RentalOpened).IsSealed);
    }
}
