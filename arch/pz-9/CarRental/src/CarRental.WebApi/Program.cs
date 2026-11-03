using CarRental.Application;
using CarRental.Application.Common;
using CarRental.Application.Rentals.Pricing;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Infrastructure;
using CarRental.Infrastructure.Persistence;
using CarRental.WebApi;
using CarRental.WebApi.Mapping;
using CarRental.WebApi.Validation;
using FluentValidation;
using FluentValidation.AspNetCore;
using Microsoft.EntityFrameworkCore;

// Точка складання: єдине місце, де відомі конкретні типи всіх шарів і життєві цикли компонентів.
var builder = WebApplication.CreateBuilder(args);

// Контейнер перевіряє життєві цикли: Singleton не може залежати від Scoped,
// а всі залежності з'ясовуються вже під час запуску (а не під час першого запиту).
builder.Host.UseDefaultServiceProvider(o =>
{
    o.ValidateScopes = true;
    o.ValidateOnBuild = true;
});

builder.Services.AddControllers();

// Автоматична перевірка запитів до виклику дії контролера. Цей конвеєр синхронний, тому правила з БД живуть у валідаторах команд.
builder.Services.AddFluentValidationAutoValidation();
builder.Services.AddValidatorsFromAssemblyContaining<OpenRentalRequestValidator>();

var databasePath = builder.Configuration["CarRental:DatabasePath"]
    ?? Path.Combine(builder.Environment.ContentRootPath, "carrental.db");
var auditLogPath = builder.Configuration["CarRental:AuditLogPath"] ?? "audit.log";

builder.Services
    .AddApplication(weekendSurcharge: builder.Configuration.GetValue<bool>("Pricing:WeekendSurcharge"))
    .AddInfrastructure(o => o.UseSqlite($"Data Source={databasePath}"), auditLogPath,
        builder.Configuration["Memcached:Host"] ?? "memcached", builder.Configuration.GetValue("Memcached:Port", 11211))
    .AddAutoMapper(typeof(ApiProfile).Assembly);

var app = builder.Build();

// Початкова схема й демонстраційні автомобілі. Для змін схеми знадобляться міграції.
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<CarRentalDbContext>();
    db.Database.EnsureCreated();
    if (!db.Cars.Any())
    {
        db.Cars.AddRange(
            Car.Register(new LicensePlate("AA0001BB"), "Skoda Fabia", CarClass.Economy, new Money(800), 42_000),
            Car.Register(new LicensePlate("BB2222CC"), "Toyota Camry", CarClass.Comfort, new Money(1400), 31_500),
            Car.Register(new LicensePlate("CC3333DD"), "BMW 520d", CarClass.Premium, new Money(2500), 18_200));
        db.SaveChanges();
    }
}

app.UseMiddleware<ErrorHandlingMiddleware>();
app.MapGet("/", () => "CarRental API працює. Маршрути: /api/cars, /api/renters, /api/rentals");
app.MapControllers();

// Діагностика життєвих циклів: лише в Development, у робочому режимі ендпоінта немає.
if (app.Environment.IsDevelopment())
{
    app.MapGet("/api/_diag/lifetimes", (HttpContext http, IClock singleton, IPricingService transientA, IPricingService transientB) =>
    {
        // Один і той самий Scoped-компонент двічі за один запит: екземпляр має збігатися.
        var scopedA = http.RequestServices.GetRequiredService<CarRentalDbContext>();
        var scopedB = http.RequestServices.GetRequiredService<CarRentalDbContext>();
        return new
        {
            singletonClock = Id(singleton),
            scopedDbContext_first = Id(scopedA),
            scopedDbContext_second = Id(scopedB),
            transientPricing_first = Id(transientA),
            transientPricing_second = Id(transientB)
        };
    });
}

app.Run();

static string Id(object instance) => $"{instance.GetType().Name}#{System.Runtime.CompilerServices.RuntimeHelpers.GetHashCode(instance)}";
