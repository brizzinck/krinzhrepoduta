using System.Text.Json;
using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Common.Audit;

/// <summary>Журнал аудиту: дописує кожну подію прокату окремим рядком у файл.</summary>
public sealed class AuditLogHandler(string path) :
    IEventHandler<RentalOpened>, IEventHandler<RentalCompleted>, IEventHandler<RentalCancelled>
{
    private static readonly SemaphoreSlim Gate = new(1, 1);

    public Task HandleAsync(RentalOpened evt, CancellationToken ct) => Write(evt, ct);
    public Task HandleAsync(RentalCompleted evt, CancellationToken ct) => Write(evt, ct);
    public Task HandleAsync(RentalCancelled evt, CancellationToken ct) => Write(evt, ct);

    private async Task Write<TEvent>(TEvent evt, CancellationToken ct)
    {
        var line = $"{DateTime.UtcNow:O} {typeof(TEvent).Name} {JsonSerializer.Serialize(evt)}{Environment.NewLine}";
        await Gate.WaitAsync(ct);
        try { await File.AppendAllTextAsync(path, line, ct); }
        finally { Gate.Release(); }
    }
}
