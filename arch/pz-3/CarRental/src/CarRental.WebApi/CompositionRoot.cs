using CarRental.Application.Common.Audit;
using CarRental.Application.Common.Events;
using CarRental.Application.Fleet;
using CarRental.Application.Fleet.Handlers;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.Handlers;

namespace CarRental.WebApi;

/// <summary>Ручне складання шини подій: хто на які події підписаний. Усі зв'язки видно в одному місці.</summary>
public static class CompositionRoot
{
    public static IEventBus BuildEventBus(IServiceProvider sp)
    {
        var bus = new InProcessEventBus();
        var audit = new AuditLogHandler(sp.GetRequiredService<IAuditLog>());

        bus.Subscribe<RentalOpened>(new NotifyRenterOnRentalOpened(
            sp.GetRequiredService<IRenterStore>(), sp.GetRequiredService<INotificationService>()));
        bus.Subscribe<RentalOpened>(new TrackRentalOnRentalOpened(sp.GetRequiredService<IAnalyticsService>()));
        bus.Subscribe<RentalOpened>(audit);

        bus.Subscribe<RentalCompleted>(new UpdateCarMileageOnRentalCompleted(sp.GetRequiredService<ICarStore>()));
        bus.Subscribe<RentalCompleted>(audit);

        bus.Subscribe<RentalCancelled>(audit);
        return bus;
    }
}
