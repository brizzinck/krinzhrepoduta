using CarRental.Application.Common.Audit;
using CarRental.Application.Common.Caching;
using CarRental.Application.Common.Events;
using CarRental.Application.Fleet;
using CarRental.Application.Fleet.Handlers;
using CarRental.Application.Mapping;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.Handlers;
using CarRental.Application.Rentals.Pricing;
using CarRental.Application.Rentals.UseCases;
using CarRental.Application.Validation;
using FluentValidation;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace CarRental.Application;

/// <summary>Реєстрація компонентів шару Application. Конкретні реалізації портів додає AddInfrastructure.</summary>
public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services, bool weekendSurcharge = false)
    {
        // Scoped: сценарії працюють з Scoped-сховищами, тож живуть один запит.
        services.AddScoped<CarService>();
        services.AddScoped<RenterService>();
        services.AddScoped<OpenRentalHandler>();
        services.AddScoped<CompleteRentalHandler>();
        services.AddScoped<CancelRentalHandler>();
        services.AddScoped<RentalQueryService>();
        // Декоратор над сервісом читання: контролери отримують версію з кешем.
        services.AddScoped<IRentalQueryService>(sp => new CachedRentalQueryService(
            sp.GetRequiredService<RentalQueryService>(), sp.GetRequiredService<ICacheStore>(),
            sp.GetRequiredService<ILogger<CachedRentalQueryService>>()));

        // Transient: легкі компоненти без стану; кожен споживач отримує власний екземпляр.
        services.AddTransient<IPricingService, PricingService>();
        services.AddTransient<IPricingRule, ClassMultiplierRule>();
        services.AddTransient<IPricingRule, LongRentalDiscountRule>();
        if (weekendSurcharge)
            services.AddTransient<IPricingRule, WeekendSurchargeRule>();

        // Профілі мапінгу цієї збірки. Конфігурація одна на застосунок, IMapper створюється контейнером.
        services.AddAutoMapper(typeof(CarProfile).Assembly);

        // Валідатори команд: Scoped, бо читають дані через Unit of Work.
        services.AddValidatorsFromAssemblyContaining<OpenRentalCommandValidator>();

        // Обробники подій: Scoped, бо працюють зі Scoped-сховищами. Порядок реєстрації = порядок виклику.
        services.AddScoped<IEventHandler<RentalOpened>, NotifyRenterOnRentalOpened>();
        services.AddScoped<IEventHandler<RentalOpened>, TrackRentalOnRentalOpened>();
        services.AddScoped<IEventHandler<RentalOpened>, AuditLogHandler>();
        // Інвалідація першою: збій наступного обробника не лишить у кеші застарілих даних.
        services.AddScoped<IEventHandler<RentalCompleted>, InvalidateRentalCache>();
        services.AddScoped<IEventHandler<RentalCompleted>, UpdateCarMileageOnRentalCompleted>();
        services.AddScoped<IEventHandler<RentalCompleted>, AuditLogHandler>();
        services.AddScoped<IEventHandler<RentalCancelled>, InvalidateRentalCache>();
        services.AddScoped<IEventHandler<RentalCancelled>, AuditLogHandler>();
        return services;
    }
}
