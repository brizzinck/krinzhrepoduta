namespace CarRental.Application.Common.Events;

/// <summary>
/// Шина подій у межах одного процесу. Обробники реєструються явно (Subscribe),
/// публікація викликає всі обробники події послідовно в порядку підписки.
/// </summary>
public sealed class InProcessEventBus : IEventBus
{
    private readonly Dictionary<Type, List<object>> _handlers = [];

    public void Subscribe<TEvent>(IEventHandler<TEvent> handler)
    {
        ArgumentNullException.ThrowIfNull(handler);
        if (!_handlers.TryGetValue(typeof(TEvent), out var list))
            _handlers[typeof(TEvent)] = list = [];
        list.Add(handler);
    }

    public async Task PublishAsync<TEvent>(TEvent evt, CancellationToken ct)
    {
        if (!_handlers.TryGetValue(typeof(TEvent), out var list))
            return; // Подія без підписників теж допустима.
        foreach (var handler in list.ToArray())
            await ((IEventHandler<TEvent>)handler).HandleAsync(evt, ct);
    }
}
