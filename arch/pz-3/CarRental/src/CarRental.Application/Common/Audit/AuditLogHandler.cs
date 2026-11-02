using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Common.Audit;

/// <summary>Підписник, що передає кожну подію прокату в журнал аудиту.</summary>
public sealed class AuditLogHandler(IAuditLog log) :
    IEventHandler<RentalOpened>, IEventHandler<RentalCompleted>, IEventHandler<RentalCancelled>
{
    public Task HandleAsync(RentalOpened evt, CancellationToken ct) => log.WriteAsync(nameof(RentalOpened), evt, ct);
    public Task HandleAsync(RentalCompleted evt, CancellationToken ct) => log.WriteAsync(nameof(RentalCompleted), evt, ct);
    public Task HandleAsync(RentalCancelled evt, CancellationToken ct) => log.WriteAsync(nameof(RentalCancelled), evt, ct);
}
