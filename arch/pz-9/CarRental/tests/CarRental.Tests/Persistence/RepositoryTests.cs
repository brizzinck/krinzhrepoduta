using CarRental.Application.Common;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet;
using CarRental.Application.Rentals;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;
using CarRental.Tests.Integration;

namespace CarRental.Tests.Persistence;

/// <summary>Репозиторії на SQLite: загальні операції та запити предметної області.</summary>
public class RepositoryTests
{
    private static readonly DateOnly Today = DateOnly.FromDateTime(DateTime.UtcNow);

    private static Car NewCar(string plate = "AA1234BB", decimal rate = 800m) =>
        Car.Register(new LicensePlate(plate), "Skoda Fabia", CarClass.Economy, new Money(rate), 10_000);

    private static Renter NewRenter() =>
        Renter.Register("Іваненко Іван Іванович", "ivan@example.com", new DateOnly(1990, 5, 20),
            new DriverLicense("ABC123456", Today.AddYears(5)), Today);

    [Fact]
    public async Task Repository_supports_add_get_update_and_delete()
    {
        using var f = new SqliteFixture();
        var car = NewCar();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            await uow.Cars.AddAsync(car, default);
            await uow.SaveChangesAsync(default);
        }
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            var loaded = await uow.Cars.GetByIdAsync(car.Id, default);
            Assert.Equal("AA1234BB", loaded!.Plate.Value);
            loaded.RecordMileage(10_500); // відстежуваний об'єкт зберігається без виклику Update
            await uow.SaveChangesAsync(default);
        }
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            var loaded = (await uow.Cars.GetByIdAsync(car.Id, default))!;
            Assert.Equal(10_500, loaded.Mileage);
            uow.Cars.Delete(loaded);
            await uow.SaveChangesAsync(default);
        }
        await using var check = f.NewContext();
        Assert.Null(await check.Get<IUnitOfWork>().Cars.GetByIdAsync(car.Id, default));
    }

    [Fact]
    public async Task Update_persists_changes_of_an_entity_detached_from_tracking()
    {
        using var f = new SqliteFixture();
        var car = NewCar();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            await uow.Cars.AddAsync(car, default);
            await uow.SaveChangesAsync(default);
        }
        Car detached;
        await using (var request = f.NewContext())
            detached = (await request.Get<IUnitOfWork>().Cars.GetAllAsync(default)).Single(); // без відстеження
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            uow.Cars.Update(detached); // спершу приєднуємо: версія на цей момент стає очікуваною
            detached.RecordMileage(12_000);
            await uow.SaveChangesAsync(default);
        }
        await using var check = f.NewContext();
        Assert.Equal(12_000, (await check.Get<IUnitOfWork>().Cars.GetByIdAsync(car.Id, default))!.Mileage);
    }

    [Fact]
    public async Task Update_of_a_stale_detached_entity_is_rejected_as_a_concurrent_change()
    {
        using var f = new SqliteFixture();
        var car = NewCar();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            await uow.Cars.AddAsync(car, default);
            await uow.SaveChangesAsync(default);
        }
        Car stale;
        await using (var request = f.NewContext())
            stale = (await request.Get<IUnitOfWork>().Cars.GetAllAsync(default)).Single();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            (await uow.Cars.GetByIdAsync(car.Id, default))!.RecordMileage(11_000);
            await uow.SaveChangesAsync(default);
        }
        await using var last = f.NewContext();
        var lastUow = last.Get<IUnitOfWork>();
        lastUow.Cars.Update(stale);
        stale.RecordMileage(12_000);
        await Assert.ThrowsAsync<ConcurrentChangeException>(() => lastUow.SaveChangesAsync(default));
    }

    [Fact]
    public async Task Car_queries_find_by_plate_and_filter_available_cars()
    {
        using var f = new SqliteFixture();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            var maintenance = NewCar("BB0002CC");
            maintenance.SendToMaintenance();
            await uow.Cars.AddAsync(NewCar("CC0003DD"), default);
            await uow.Cars.AddAsync(maintenance, default);
            await uow.Cars.AddAsync(NewCar("AA0001BB"), default);
            await uow.SaveChangesAsync(default);
        }
        await using var check = f.NewContext();
        var cars = check.Get<ICarRepository>();
        Assert.True(await cars.PlateExistsAsync(new LicensePlate("bb0002cc"), default));
        Assert.False(await cars.PlateExistsAsync(new LicensePlate("ZZ9999ZZ"), default));
        Assert.Equal("BB0002CC", (await cars.FindByPlateAsync(new LicensePlate("BB0002CC"), default))!.Plate.Value);
        // Список впорядковано за номером; авто на обслуговуванні не потрапляє до доступних.
        Assert.Equal(["AA0001BB", "BB0002CC", "CC0003DD"], (await cars.GetAllAsync(default)).Select(c => c.Plate.Value));
        Assert.Equal(["AA0001BB", "CC0003DD"], (await cars.ListAvailableAsync(default)).Select(c => c.Plate.Value));
    }

    [Fact]
    public async Task Rental_queries_filter_by_renter_and_return_only_active_periods_of_a_car()
    {
        using var f = new SqliteFixture();
        var renter1 = NewRenter();
        var renter2 = NewRenter();
        var car = NewCar();
        var rentable = new RentableCar(car.Id, car.DailyRate, car.Class, car.Mileage, true);
        Rental Open(Renter renter, int offset) => Rental.Open(rentable, renter,
            new RentalPeriod(Today.AddDays(offset), Today.AddDays(offset + 2)), new Money(1600m), Today, []);
        var first = Open(renter1, 10);
        var second = Open(renter2, 1);
        var third = Open(renter1, 20);
        third.Cancel(Today);
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            await uow.Cars.AddAsync(car, default);
            await uow.Renters.AddAsync(renter1, default);
            await uow.Renters.AddAsync(renter2, default);
            foreach (var rental in new[] { first, second, third })
                await uow.Rentals.AddAsync(rental, default);
            await uow.SaveChangesAsync(default);
        }
        await using var check = f.NewContext();
        var rentals = check.Get<IRentalRepository>();
        Assert.Equal([second.Id, first.Id, third.Id], (await rentals.ListByRenterAsync(null, default)).Select(r => r.Id));
        Assert.Equal([first.Id, third.Id], (await rentals.ListByRenterAsync(renter1.Id, default)).Select(r => r.Id));
        Assert.Equal(3, (await rentals.GetAllAsync(default)).Count);
        // Скасована оренда не блокує дати.
        Assert.Equal(2, (await rentals.ActivePeriodsOfCarAsync(car.Id, default)).Count);
    }

    [Fact]
    public async Task GetAll_returns_entities_without_change_tracking()
    {
        using var f = new SqliteFixture();
        await using (var request = f.NewContext())
        {
            var uow = request.Get<IUnitOfWork>();
            await uow.Cars.AddAsync(NewCar(), default);
            await uow.SaveChangesAsync(default);
        }
        await using var check = f.NewContext();
        var uow2 = check.Get<IUnitOfWork>();
        (await uow2.Cars.GetAllAsync(default)).Single().RecordMileage(99_999);
        Assert.Equal(0, await uow2.SaveChangesAsync(default)); // зміна не відстежується, нічого не збережено
    }
}
