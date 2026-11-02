using CarRental.Application.Common;
using CarRental.Application.Common.Audit;
using CarRental.Application.Common.Events;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Pricing;
using CarRental.Application.Rentals.UseCases;
using CarRental.Infrastructure.Persistence;
using CarRental.Tests.Integration;
using Microsoft.Extensions.DependencyInjection;

namespace CarRental.Tests.Di;

/// <summary>Контейнер DI: усі компоненти створюються, життєві цикли відповідають задуму.</summary>
public class DependencyInjectionTests
{
    [Fact]
    public void Every_use_case_and_port_is_resolvable_from_the_container()
    {
        using var f = new SqliteFixture(); // ValidateOnBuild перевірив усі реєстрації під час побудови
        using var request = f.NewContext();
        foreach (var type in new[]
                 {
                     typeof(CarService), typeof(RenterService), typeof(OpenRentalHandler), typeof(CompleteRentalHandler),
                     typeof(CancelRentalHandler), typeof(RentalQueryService), typeof(IPricingService), typeof(IRentalMapper),
                     typeof(ICarStore), typeof(IRenterStore), typeof(IRentalStore), typeof(IRentalReader),
                     typeof(IFleetGateway), typeof(IEventBus), typeof(IAuditLog), typeof(IClock)
                 })
            Assert.NotNull(request.Get<IServiceProvider>().GetService(type));
    }

    [Fact]
    public void Singleton_components_are_shared_between_requests()
    {
        using var f = new SqliteFixture();
        using var first = f.NewContext();
        using var second = f.NewContext();
        Assert.Same(first.Get<IClock>(), second.Get<IClock>());
        Assert.Same(first.Get<IEventBus>(), second.Get<IEventBus>());
        Assert.Same(first.Get<IAuditLog>(), second.Get<IAuditLog>());
        Assert.Same(first.Get<IAnalyticsService>(), second.Get<IAnalyticsService>());
    }

    [Fact]
    public void Scoped_components_are_shared_inside_a_request_and_differ_between_requests()
    {
        using var f = new SqliteFixture();
        using var first = f.NewContext();
        using var second = f.NewContext();
        Assert.Same(first.Get<CarRentalDbContext>(), first.Get<CarRentalDbContext>());
        Assert.Same(first.Get<ICarStore>(), first.Get<ICarStore>());
        Assert.NotSame(first.Get<CarRentalDbContext>(), second.Get<CarRentalDbContext>());
        Assert.NotSame(first.Get<OpenRentalHandler>(), second.Get<OpenRentalHandler>());
        // IRentalReader і IRentalStore в межах запиту це один і той самий об'єкт.
        Assert.Same(first.Get<IRentalStore>(), first.Get<IRentalReader>());
    }

    [Fact]
    public void Transient_components_are_created_anew_on_every_resolution()
    {
        using var f = new SqliteFixture();
        using var request = f.NewContext();
        Assert.NotSame(request.Get<IPricingService>(), request.Get<IPricingService>());
        Assert.NotSame(request.Get<IRentalMapper>(), request.Get<IRentalMapper>());
    }

    [Fact]
    public void Pricing_rules_are_resolved_as_a_collection_in_registration_order()
    {
        using var f = new SqliteFixture();
        using var request = f.NewContext();
        var rules = request.Get<IEnumerable<IPricingRule>>().Select(r => r.GetType()).ToArray();
        Assert.Equal([typeof(ClassMultiplierRule), typeof(LongRentalDiscountRule)], rules);
    }

    [Fact]
    public void Singleton_that_depends_on_scoped_is_rejected_by_scope_validation()
    {
        // Навмисно хибна реєстрація (лише в тесті): Singleton бере Scoped-сховище.
        var services = new ServiceCollection();
        services.AddScoped<IRenterStore, FakeRenterStore>();
        services.AddSingleton<BrokenSingleton>();
        using var provider = services.BuildServiceProvider(new ServiceProviderOptions { ValidateScopes = true });
        using var scope = provider.CreateScope();
        var error = Assert.Throws<InvalidOperationException>(() => provider.GetRequiredService<BrokenSingleton>());
        Assert.Contains("scoped", error.Message, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public void Missing_registration_is_found_at_startup_by_validate_on_build()
    {
        var services = new ServiceCollection();
        services.AddScoped<BrokenSingleton>(); // залежність IRenterStore не зареєстровано
        Assert.ThrowsAny<Exception>(() =>
            services.BuildServiceProvider(new ServiceProviderOptions { ValidateOnBuild = true }));
    }

    [Fact]
    public void Application_classes_depend_only_on_abstractions()
    {
        var concrete = typeof(OpenRentalHandler).Assembly.GetTypes()
            .Where(t => t is { IsClass: true, IsAbstract: false } && !t.IsNested && t.IsPublic)
            .SelectMany(t => t.GetConstructors().SelectMany(c => c.GetParameters().Select(p => (Type: t, Param: p.ParameterType))))
            .Where(x => !IsAbstraction(x.Param))
            .Select(x => $"{x.Type.Name}({x.Param.Name})")
            .ToArray();
        Assert.Empty(concrete);
    }

    private static bool IsAbstraction(Type type) =>
        type.IsInterface
        || type.IsGenericType && type.GetGenericArguments().All(a => a.IsInterface)
        // Прості конфігураційні типи (рядки, числа) та записи команд не є службами.
        || type == typeof(string) || type.IsValueType
        || type.Namespace?.EndsWith(".Rentals") == true && type.Name.EndsWith("Command")
        || type.Namespace?.StartsWith("CarRental.Domain") == true;

    private sealed class FakeRenterStore : IRenterStore
    {
        public Task<CarRental.Domain.Rentals.Renter?> Find(Guid id, CancellationToken ct) => Task.FromResult<CarRental.Domain.Rentals.Renter?>(null);
        public Task Add(CarRental.Domain.Rentals.Renter renter, CancellationToken ct) => Task.CompletedTask;
        public Task Save(CancellationToken ct) => Task.CompletedTask;
    }

    private sealed class BrokenSingleton(IRenterStore store)
    {
        public IRenterStore Store { get; } = store;
    }
}
