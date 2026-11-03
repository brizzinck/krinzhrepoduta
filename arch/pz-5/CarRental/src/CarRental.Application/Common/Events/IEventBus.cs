namespace CarRental.Application.Common.Events;

/// <summary>Шина подій: ініціатор публікує факт і нічого не знає про те, хто на нього реагує.</summary>
public interface IEventBus
{
    Task PublishAsync<TEvent>(TEvent evt, CancellationToken ct);
}
