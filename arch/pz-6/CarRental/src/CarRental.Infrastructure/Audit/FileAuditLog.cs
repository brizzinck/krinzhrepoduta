using System.Text.Json;
using CarRental.Application.Common;
using CarRental.Application.Common.Audit;

namespace CarRental.Infrastructure.Audit;

/// <summary>Реалізація журналу аудиту: один рядок на подію у текстовому файлі. Живе як Singleton.</summary>
public sealed class FileAuditLog(string path, IClock clock) : IAuditLog
{
    // Стан належить екземпляру (а не типу): один Singleton серіалізує запис у файл.
    private readonly SemaphoreSlim _gate = new(1, 1);

    public async Task WriteAsync(string eventName, object payload, CancellationToken ct)
    {
        var line = $"{clock.UtcNow:O} {eventName} {JsonSerializer.Serialize(payload, payload.GetType())}{Environment.NewLine}";
        await _gate.WaitAsync(ct);
        try { await File.AppendAllTextAsync(path, line, ct); }
        finally { _gate.Release(); }
    }
}
