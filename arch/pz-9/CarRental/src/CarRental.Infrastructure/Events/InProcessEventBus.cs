using CarRental.Application.Common.Events;
using Microsoft.Extensions.DependencyInjection;

namespace CarRental.Infrastructure.Events;

/// <summary>
/// Шина подій у межах одного процесу. Живе як Singleton, тому не тримає Scoped-залежностей:
/// на кожну публікацію вона створює власну область (scope) і бере з неї всі обробники події.
/// Це єдине місце, де контейнер використовується напряму: шина резолвить обробники за типом події,
/// якого не знає під час компіляції. Бізнес-логіка про контейнер нічого не знає.
/// </summary>
public sealed class InProcessEventBus(IServiceScopeFactory scopes) : IEventBus
{
    public async Task PublishAsync<TEvent>(TEvent evt, CancellationToken ct)
    {
        await using var scope = scopes.CreateAsyncScope();
        // Обробники виконуються послідовно в порядку їх реєстрації в контейнері.
        foreach (var handler in scope.ServiceProvider.GetServices<IEventHandler<TEvent>>())
            await handler.HandleAsync(evt, ct);
    }
}
