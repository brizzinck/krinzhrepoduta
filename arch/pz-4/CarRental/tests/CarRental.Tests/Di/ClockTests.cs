using CarRental.Application.Rentals;
using CarRental.Domain.Common;
using CarRental.Tests.Integration;

namespace CarRental.Tests.Di;

/// <summary>Час як залежність: тести керують «сьогодні» й перевіряють правила, що від нього залежать.</summary>
public class ClockTests
{
    private static async Task<(Guid car, Guid renter)> SeedAsync(SqliteFixture f)
    {
        await using var db = f.NewContext();
        var car = await f.Cars(db).Register("AA1234BB", "Skoda Fabia", CarClass.Economy, 800m, 10_000);
        var renter = await f.Renters(db).Register("Іваненко Іван Іванович", "ivan@example.com",
            new DateOnly(1990, 5, 20), "abc123456", new DateOnly(2040, 1, 1));
        return (car.Id, renter.Id);
    }

    [Fact]
    public async Task Open_uses_the_injected_clock_for_the_start_in_the_past_rule()
    {
        using var f = new SqliteFixture();
        f.Clock.UtcNow = new DateTime(2030, 6, 10, 12, 0, 0, DateTimeKind.Utc);
        var (car, renter) = await SeedAsync(f);
        await using var db = f.NewContext();
        // 2030-06-09 пізніше за реальний сьогоднішній день, але раніше за «сьогодні» годинника.
        var error = await Assert.ThrowsAsync<DomainRuleException>(() => f.Rentals(db)
            .Open(new OpenRentalCommand(car, renter, new DateOnly(2030, 6, 9), new DateOnly(2030, 6, 12))));
        Assert.Equal("Оренда не може починатися в минулому.", error.Message);

        var rental = await f.Rentals(db)
            .Open(new OpenRentalCommand(car, renter, new DateOnly(2030, 6, 10), new DateOnly(2030, 6, 12)));
        Assert.Equal(new DateOnly(2030, 6, 10), rental.Start);
    }

    [Fact]
    public async Task Cancel_uses_the_injected_clock_for_the_before_start_rule()
    {
        using var f = new SqliteFixture();
        f.Clock.UtcNow = new DateTime(2030, 6, 1, 8, 0, 0, DateTimeKind.Utc);
        var (car, renter) = await SeedAsync(f);
        Guid id;
        await using (var db = f.NewContext())
            id = (await f.Rentals(db).Open(new OpenRentalCommand(car, renter, new DateOnly(2030, 6, 5), new DateOnly(2030, 6, 8)))).Id;

        f.Clock.UtcNow = new DateTime(2030, 6, 5, 8, 0, 0, DateTimeKind.Utc); // день початку: скасувати вже не можна
        await using var late = f.NewContext();
        await Assert.ThrowsAsync<DomainRuleException>(() => f.Rentals(late).Cancel(id));
    }

    [Fact]
    public async Task Renter_registration_takes_today_from_the_clock()
    {
        using var f = new SqliteFixture();
        f.Clock.UtcNow = new DateTime(2030, 1, 1, 0, 0, 0, DateTimeKind.Utc);
        await using var db = f.NewContext();
        // Дата народження 2030-01-02 пізніша за «сьогодні» годинника (2030-01-01), тому вона некоректна.
        await Assert.ThrowsAsync<DomainRuleException>(() => f.Renters(db).Register("Іваненко Іван Іванович",
            "ivan@example.com", new DateOnly(2030, 1, 2), "abc123456", new DateOnly(2040, 1, 1)));
    }

    [Fact]
    public async Task Audit_log_timestamps_come_from_the_clock()
    {
        using var f = new SqliteFixture();
        f.Clock.UtcNow = new DateTime(2030, 6, 1, 8, 30, 15, DateTimeKind.Utc);
        var (car, renter) = await SeedAsync(f);
        await using var db = f.NewContext();
        await f.Rentals(db).Open(new OpenRentalCommand(car, renter, new DateOnly(2030, 6, 5), new DateOnly(2030, 6, 8)));
        var line = Assert.Single(await File.ReadAllLinesAsync(f.AuditPath));
        Assert.StartsWith("2030-06-01T08:30:15.0000000Z RentalOpened", line);
    }
}
