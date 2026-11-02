namespace CarRental.Application.Common.Audit;

/// <summary>Журнал аудиту як абстракція: куди й у якому форматі пишеться, вирішує Infrastructure.</summary>
public interface IAuditLog
{
    Task WriteAsync(string eventName, object payload, CancellationToken ct);
}
