using CarRental.Application.Common.Events;
using CarRental.Application.Rentals.Events;

namespace CarRental.Application.Rentals.Handlers;

/// <summary>Реакція «врахувати оренду в аналітиці».</summary>
public sealed class TrackRentalOnRentalOpened(IAnalyticsService analytics) : IEventHandler<RentalOpened>
{
    public Task HandleAsync(RentalOpened evt, CancellationToken ct) =>
        analytics.TrackRentalOpenedAsync(evt.RentalId, evt.Total, ct);
}
