using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;

using DomainCar = CarRental.Domain.Fleet.Car;
using DomainRenter = CarRental.Domain.Rentals.Renter;
using DomainRental = CarRental.Domain.Rentals.Rental;

namespace CarRental.Tests;

/// <summary>Фабрики типових доменних об'єктів для тестів.</summary>
internal static class TestData
{
    public static readonly DateOnly Today = new(2026, 11, 2);

    public static DomainCar Car(CarClass carClass = CarClass.Economy, decimal rate = 800m, int mileage = 10_000) =>
        DomainCar.Register(new LicensePlate("AA1234BB"), "Skoda Fabia", carClass, new Money(rate), mileage);

    public static RentableCar RentableCar(CarClass carClass = CarClass.Economy, decimal rate = 800m,
        int mileage = 10_000, bool rentable = true) =>
        new(Guid.NewGuid(), new Money(rate), carClass, mileage, rentable);

    public static DomainRenter Renter(DateOnly? birth = null, DateOnly? licenseUntil = null) =>
        DomainRenter.Register("Іваненко Іван Іванович", "ivan@example.com", birth ?? new DateOnly(1990, 5, 20),
            new DriverLicense("ABC123456", licenseUntil ?? new DateOnly(2035, 1, 1)), Today);

    public static RentalPeriod Period(int startOffsetDays = 1, int days = 3) =>
        new(Today.AddDays(startOffsetDays), Today.AddDays(startOffsetDays + days));

    public static DomainRental OpenRental(RentableCar? car = null, RentalPeriod? period = null) =>
        DomainRental.Open(car ?? RentableCar(), Renter(), period ?? Period(), new Money(2400m), Today, []);
}
