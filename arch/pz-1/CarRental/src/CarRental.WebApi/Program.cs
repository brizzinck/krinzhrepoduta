using System.Text.Json.Serialization;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Infrastructure.Integration;
using CarRental.Infrastructure.Notifications;
using CarRental.Infrastructure.Persistence;
using CarRental.WebApi;
using Microsoft.EntityFrameworkCore;

// Точка складання: єдине місце, де відомі конкретні типи всіх шарів.
var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers().AddJsonOptions(o =>
    o.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter()));

var databasePath = builder.Configuration["CarRental:DatabasePath"]
    ?? Path.Combine(builder.Environment.ContentRootPath, "carrental.db");
builder.Services.AddDbContext<CarRentalDbContext>(o => o.UseSqlite($"Data Source={databasePath}"));

// Реалізації Infrastructure для контрактів Application.
builder.Services.AddScoped<ICarStore, SqliteCarStore>();
builder.Services.AddScoped<IRenterStore, SqliteRenterStore>();
builder.Services.AddScoped<IRentalStore, SqliteRentalStore>();
builder.Services.AddScoped<IFleetGateway, FleetGateway>();
builder.Services.AddScoped<INotificationService, ConsoleNotificationService>();
builder.Services.AddSingleton<IAnalyticsService, InMemoryAnalyticsService>();
builder.Services.AddScoped<CarService>();
builder.Services.AddScoped<RenterService>();
builder.Services.AddScoped<RentalService>();

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
app.Run();
