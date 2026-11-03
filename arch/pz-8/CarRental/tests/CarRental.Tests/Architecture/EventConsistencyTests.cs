using CarRental.Application.Common.Events;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Events;
using CarRental.Domain.Common;
using CarRental.Domain.Rentals;
using CarRental.Tests.Integration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;

namespace CarRental.Tests.Architecture;

/// <summary>Фіксує поточну поведінку, описану в аналізі ПЗ 7: події виконуються після фіксації, без outbox.</summary>
public class EventConsistencyTests
{
    private sealed class FailingHandler : IEventHandler<RentalCompleted>
    {
        public Task HandleAsync(RentalCompleted evt, CancellationToken ct) =>
            throw new InvalidOperationException("Збій обробника");
    }

    [Fact]
    public async Task Handler_failure_after_commit_leaves_rental_completed_and_car_mileage_stale()
    {
        using var f = new SqliteFixture(services =>
        {
            services.RemoveAll<IEventHandler<RentalCompleted>>();
            services.AddScoped<IEventHandler<RentalCompleted>, FailingHandler>();
        });
        var today = f.Clock.Today;
        Guid carId, renterId, rentalId;
        await using (var db = f.NewContext())
        {
            carId = (await f.Cars(db).Register("AA1234BB", "Skoda Fabia", CarClass.Economy, 800m, 10_000)).Id;
            renterId = (await f.Renters(db).Register("Іваненко Іван Іванович", "ivan@example.com",
                new DateOnly(1990, 5, 20), "ABC123456", today.AddYears(5))).Id;
            rentalId = (await f.Rentals(db).Open(new OpenRentalCommand(carId, renterId, today.AddDays(1), today.AddDays(4)))).Id;
        }

        await using (var db = f.NewContext())
            await Assert.ThrowsAsync<InvalidOperationException>(() => f.Rentals(db).Complete(rentalId, 10_480));

        await using var check = f.NewContext();
        Assert.Equal("Completed", (await f.Rentals(check).Get(rentalId)).Status); // оренду вже зафіксовано
        Assert.Equal(10_000, (await f.Cars(check).Get(carId)).Mileage);           // реакція Fleet втрачена
    }

    [Fact]
    public async Task Two_requests_that_read_before_either_saves_can_book_the_same_car_twice()
    {
        using var f = new SqliteFixture();
        var today = f.Clock.Today;
        Guid carId, renterId;
        await using (var db = f.NewContext())
        {
            carId = (await f.Cars(db).Register("AA1234BB", "Skoda Fabia", CarClass.Economy, 800m, 10_000)).Id;
            renterId = (await f.Renters(db).Register("Іваненко Іван Іванович", "ivan@example.com",
                new DateOnly(1990, 5, 20), "ABC123456", today.AddYears(5))).Id;
        }
        var period = new RentalPeriod(today.AddDays(1), today.AddDays(4));

        await using var first = f.NewContext();
        await using var second = f.NewContext();
        var a = await Prepare(first);
        var b = await Prepare(second); // обидва запити ще бачать вільне авто
        await first.Get<IUnitOfWork>().SaveChangesAsync(default);
        await second.Get<IUnitOfWork>().SaveChangesAsync(default);

        await using var check = f.NewContext();
        var busy = await check.Get<IUnitOfWork>().Rentals.ActivePeriodsOfCarAsync(carId, default);
        Assert.NotEqual(a.Id, b.Id);
        Assert.Equal(2, busy.Count); // обмеження на перетин у БД немає, перевірку робить лише домен

        async Task<Rental> Prepare(Request request)
        {
            var uow = request.Get<IUnitOfWork>();
            var car = await request.Get<IFleetGateway>().GetRentableCar(carId, default);
            var renter = await uow.Renters.GetByIdAsync(renterId, default);
            var active = await uow.Rentals.ActivePeriodsOfCarAsync(carId, default);
            var rental = Rental.Open(car!, renter!, period, new Money(2400m), today, active);
            await uow.Rentals.AddAsync(rental, default);
            return rental;
        }
    }
}
