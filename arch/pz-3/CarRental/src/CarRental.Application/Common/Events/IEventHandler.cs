namespace CarRental.Application.Common.Events;

/// <summary>Обробник однієї події: одна реакція системи на факт, що відбувся.</summary>
public interface IEventHandler<in TEvent>
{
    Task HandleAsync(TEvent evt, CancellationToken ct);
}
