using CarRental.Application.Common;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet;
using CarRental.Application.Fleet.Dto;
using CarRental.Application.Rentals;
using CarRental.Domain.Common;

namespace CarRental.Tests.Integration;

public class PersistenceTests
{
    private static readonly DateOnly Today = DateOnly.FromDateTime(DateTime.UtcNow);

    private static async Task<Guid> RenterAsync(SqliteFixture f, DateOnly? birth = null)
    {
        await using var db = f.NewContext();
        return (await f.Renters(db).Register("Іваненко Іван Іванович", "ivan@example.com",
            birth ?? new DateOnly(1990, 5, 20), "abc123456", Today.AddYears(5))).Id;
    }

    private static async Task<CarDto> CarAsync(SqliteFixture f, string plate = "aa1234bb",
        CarClass carClass = CarClass.Economy, decimal rate = 800m)
    {
        await using var db = f.NewContext();
        return await f.Cars(db).Register(plate, "Skoda Fabia", carClass, rate, 10_000);
    }

    [Fact]
    public async Task Car_survives_a_new_DbContext_with_normalized_plate()
    {
        using var f = new SqliteFixture();
        var created = await CarAsync(f);
        await using var db = f.NewContext();
        var loaded = await f.Cars(db).Get(created.Id);
        Assert.Equal("AA1234BB", loaded.Plate);
        Assert.Equal(800m, loaded.DailyRate);
        Assert.Equal("Economy", loaded.Class);
    }

    [Fact]
    public async Task Duplicate_plate_is_rejected_as_conflict()
    {
        using var f = new SqliteFixture();
        await CarAsync(f, "AA1234BB");
        await using var db = f.NewContext();
        await Assert.ThrowsAsync<DomainConflictException>(() =>
            f.Cars(db).Register("aa1234bb", "Another", CarClass.Comfort, 1000m, 0));
    }

    [Fact]
    public async Task Renter_with_license_value_object_is_persisted()
    {
        using var f = new SqliteFixture();
        var id = await RenterAsync(f);
        await using var db = f.NewContext();
        var renter = await f.Renters(db).Get(id);
        Assert.Equal("ABC123456", renter.LicenseNumber);
        Assert.Equal(Today.AddYears(5), renter.LicenseValidUntil);
    }

    [Fact]
    public async Task Rental_is_persisted_with_period_and_total()
    {
        using var f = new SqliteFixture();
        var car = await CarAsync(f);
        var renter = await RenterAsync(f);
        Guid id;
        await using (var db = f.NewContext())
            id = (await f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(1), Today.AddDays(4)))).Id;

        await using var db2 = f.NewContext();
        var rental = await f.Rentals(db2).Get(id);
        Assert.Equal(Today.AddDays(1), rental.Period.Start);
        Assert.Equal(3, rental.Period.Days);
        Assert.Equal(2400m, rental.Total.Amount);
        Assert.Equal("Active", rental.Status);
    }

    [Theory]
    [InlineData(CarClass.Economy, 800, 3, 2400)]     // 800 * 1.0 * 3
    [InlineData(CarClass.Comfort, 1400, 2, 3500)]    // 1400 * 1.25 * 2
    [InlineData(CarClass.Premium, 2500, 7, 25200)]   // 2500 * 1.6 * 7 * 0.9 (знижка від 7 днів)
    public async Task Total_depends_on_class_and_duration(CarClass carClass, decimal rate, int days, decimal expected)
    {
        using var f = new SqliteFixture();
        var car = await CarAsync(f, "KA0001AB", carClass, rate);
        var renter = await RenterAsync(f);
        await using var db = f.NewContext();
        var rental = await f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(1), Today.AddDays(1 + days)));
        Assert.Equal(expected, rental.Total.Amount);
    }

    [Fact]
    public async Task Overlapping_rental_of_same_car_is_rejected_and_adjacent_is_allowed()
    {
        using var f = new SqliteFixture();
        var car = await CarAsync(f);
        var renter = await RenterAsync(f);
        await using (var db = f.NewContext())
            await f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(2), Today.AddDays(5)));

        await using (var db = f.NewContext())
            await Assert.ThrowsAsync<DomainConflictException>(() =>
                f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(4), Today.AddDays(7))));

        await using (var db = f.NewContext())
            await f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(5), Today.AddDays(7)));
    }

    [Fact]
    public async Task Cancelled_rental_frees_the_dates()
    {
        using var f = new SqliteFixture();
        var car = await CarAsync(f);
        var renter = await RenterAsync(f);
        Guid first;
        await using (var db = f.NewContext())
            first = (await f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(2), Today.AddDays(5)))).Id;
        await using (var db = f.NewContext())
            await f.Rentals(db).Cancel(first);
        await using (var db = f.NewContext())
            await f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(2), Today.AddDays(5)));
    }

    [Fact]
    public async Task Young_renter_and_missing_car_are_rejected_with_proper_errors()
    {
        using var f = new SqliteFixture();
        var car = await CarAsync(f);
        var young = await RenterAsync(f, birth: Today.AddYears(-20));
        await using var db = f.NewContext();
        var service = f.Rentals(db);
        await Assert.ThrowsAsync<DomainRuleException>(() =>
            service.Open(new OpenRentalCommand(car.Id, young, Today.AddDays(1), Today.AddDays(3))));
        await Assert.ThrowsAsync<NotFoundException>(() =>
            service.Open(new OpenRentalCommand(Guid.NewGuid(), young, Today.AddDays(1), Today.AddDays(3))));
    }

    [Fact]
    public async Task Car_in_maintenance_cannot_be_rented()
    {
        using var f = new SqliteFixture();
        var car = await CarAsync(f);
        var renter = await RenterAsync(f);
        await using (var db = f.NewContext())
            await f.Cars(db).SendToMaintenance(car.Id);
        await using (var db = f.NewContext())
            await Assert.ThrowsAsync<DomainRuleException>(() =>
                f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(1), Today.AddDays(3))));
    }

    [Fact]
    public async Task Completing_rental_updates_car_mileage()
    {
        using var f = new SqliteFixture();
        var car = await CarAsync(f);
        var renter = await RenterAsync(f);
        Guid id;
        await using (var db = f.NewContext())
            id = (await f.Rentals(db).Open(new OpenRentalCommand(car.Id, renter, Today.AddDays(1), Today.AddDays(3)))).Id;
        await using (var db = f.NewContext())
            await f.Rentals(db).Complete(id, 10_480);
        await using var check = f.NewContext();
        Assert.Equal(10_480, (await f.Cars(check).Get(car.Id)).Mileage);
        Assert.Equal("Completed", (await f.Rentals(check).Get(id)).Status);
    }

    [Fact]
    public async Task Concurrent_updates_of_one_car_are_rejected_instead_of_lost()
    {
        using var f = new SqliteFixture();
        var car = await CarAsync(f);
        await using var first = f.NewContext();
        await using var second = f.NewContext();
        var firstCopy = (await first.Get<IUnitOfWork>().Cars.GetByIdAsync(car.Id, default))!;
        var secondCopy = (await second.Get<IUnitOfWork>().Cars.GetByIdAsync(car.Id, default))!;
        firstCopy.RecordMileage(10_100);
        secondCopy.RecordMileage(10_200);
        await first.Get<IUnitOfWork>().SaveChangesAsync(default);
        await Assert.ThrowsAsync<ConcurrentChangeException>(() =>
            second.Get<IUnitOfWork>().SaveChangesAsync(default));
    }
}
