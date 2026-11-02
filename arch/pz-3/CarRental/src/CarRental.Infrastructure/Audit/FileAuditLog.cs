using System.Text.Json;
using CarRental.Application.Common.Audit;

namespace CarRental.Infrastructure.Audit;

/// <summary>Реалізація журналу аудиту: один рядок на подію у текстовому файлі.</summary>
public sealed class FileAuditLog(string path) : IAuditLog
{
    private static readonly SemaphoreSlim Gate = new(1, 1);

    public async Task WriteAsync(string eventName, object payload, CancellationToken ct)
    {
        var line = $"{DateTime.UtcNow:O} {eventName} {JsonSerializer.Serialize(payload, payload.GetType())}{Environment.NewLine}";
        await Gate.WaitAsync(ct);
        try { await File.AppendAllTextAsync(path, line, ct); }
        finally { Gate.Release(); }
    }
}
