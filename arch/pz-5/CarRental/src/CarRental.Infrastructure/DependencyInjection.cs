using CarRental.Application.Common;
using CarRental.Application.Common.Audit;
using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Infrastructure.Audit;
using CarRental.Infrastructure.Events;
using CarRental.Infrastructure.Integration;
using CarRental.Infrastructure.Notifications;
using CarRental.Infrastructure.Persistence;
using CarRental.Infrastructure.Persistence.Repositories;
using CarRental.Infrastructure.Time;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace CarRental.Infrastructure;

/// <summary>Реєстрація реалізацій портів Application. Тут єдине місце, де відомі конкретні типи Infrastructure.</summary>
public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services,
        Action<DbContextOptionsBuilder> configureDatabase, string auditLogPath)
    {
        // Singleton: без стану або зі спільним потокобезпечним станом на весь час життя застосунку.
        services.AddSingleton<IClock, SystemClock>();
        services.AddSingleton<IAnalyticsService, InMemoryAnalyticsService>();
        services.AddSingleton<IAuditLog>(sp => new FileAuditLog(auditLogPath, sp.GetRequiredService<IClock>()));
        services.AddSingleton<IEventBus, InProcessEventBus>();

        // Scoped: DbContext, репозиторії й Unit of Work на один запит. Репозиторій оренд і його «читальний»
        // інтерфейс вказують на один екземпляр, тому всі бачать зміни однієї одиниці роботи.
        services.AddDbContext<CarRentalDbContext>(configureDatabase);
        services.AddScoped<ICarRepository, CarRepository>();
        services.AddScoped<IRenterRepository, RenterRepository>();
        services.AddScoped<IRentalRepository, RentalRepository>();
        services.AddScoped<IRentalReader>(sp => sp.GetRequiredService<IRentalRepository>());
        services.AddScoped<IUnitOfWork, UnitOfWork>();
        services.AddScoped<IFleetGateway, FleetGateway>();

        // Transient: адаптер без стану.
        services.AddTransient<INotificationService, ConsoleNotificationService>();
        return services;
    }
}
