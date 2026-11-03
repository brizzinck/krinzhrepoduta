using CarRental.Domain.Common;
using CarRental.Domain.Rentals;

namespace CarRental.Tests.Domain;

public class RentalTests
{
    [Fact]
    public void Open_creates_active_rental_with_fixed_rate_and_start_mileage()
    {
        var car = TestData.RentableCar(rate: 800m, mileage: 12_000);
        var rental = Rental.Open(car, TestData.Renter(), TestData.Period(), new Money(2400m), TestData.Today, []);
        Assert.Equal(RentalStatus.Active, rental.Status);
        Assert.Equal(car.Id, rental.CarId);
        Assert.Equal(new Money(800m), rental.DailyRate);
        Assert.Equal(12_000, rental.StartMileage);
    }

    [Fact]
    public void Open_rejects_start_in_the_past() =>
        Assert.Throws<DomainRuleException>(() => Rental.Open(TestData.RentableCar(), TestData.Renter(),
            TestData.Period(startOffsetDays: -1), new Money(100m), TestData.Today, []));

    [Fact]
    public void Open_accepts_start_today() =>
        Rental.Open(TestData.RentableCar(), TestData.Renter(), TestData.Period(startOffsetDays: 0),
            new Money(100m), TestData.Today, []);

    [Fact]
    public void Open_rejects_car_that_is_not_rentable() =>
        Assert.Throws<DomainRuleException>(() => Rental.Open(TestData.RentableCar(rentable: false),
            TestData.Renter(), TestData.Period(), new Money(100m), TestData.Today, []));

    [Fact]
    public void Open_rejects_overlap_with_active_rental_as_conflict()
    {
        var busy = new[] { TestData.Period(startOffsetDays: 2, days: 3) };
        Assert.Throws<DomainConflictException>(() => Rental.Open(TestData.RentableCar(), TestData.Renter(),
            TestData.Period(startOffsetDays: 1, days: 3), new Money(100m), TestData.Today, busy));
    }

    [Fact]
    public void Open_allows_rental_right_after_another_ends()
    {
        var busy = new[] { TestData.Period(startOffsetDays: 1, days: 3) };
        Rental.Open(TestData.RentableCar(), TestData.Renter(), TestData.Period(startOffsetDays: 4, days: 2),
            new Money(100m), TestData.Today, busy);
    }

    [Fact]
    public void Complete_records_end_mileage_and_changes_status()
    {
        var rental = TestData.OpenRental(TestData.RentableCar(mileage: 10_000));
        rental.Complete(10_450);
        Assert.Equal(RentalStatus.Completed, rental.Status);
        Assert.Equal(10_450, rental.EndMileage);
    }

    [Fact]
    public void Complete_rejects_mileage_below_start_and_second_completion()
    {
        var rental = TestData.OpenRental(TestData.RentableCar(mileage: 10_000));
        Assert.Throws<DomainRuleException>(() => rental.Complete(9_000));
        rental.Complete(10_100);
        Assert.Throws<DomainRuleException>(() => rental.Complete(10_200));
    }

    [Fact]
    public void Cancel_is_allowed_only_before_start_and_only_for_active()
    {
        var rental = TestData.OpenRental(period: TestData.Period(startOffsetDays: 3));
        Assert.Throws<DomainRuleException>(() => rental.Cancel(TestData.Today.AddDays(3)));
        rental.Cancel(TestData.Today);
        Assert.Equal(RentalStatus.Cancelled, rental.Status);
        Assert.Throws<DomainRuleException>(() => rental.Cancel(TestData.Today));
    }

    [Fact]
    public void Cancelled_rental_cannot_be_completed()
    {
        var rental = TestData.OpenRental(period: TestData.Period(startOffsetDays: 3));
        rental.Cancel(TestData.Today);
        Assert.Throws<DomainRuleException>(() => rental.Complete(20_000));
    }
}
