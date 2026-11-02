using CarRental.Domain.Common;
using CarRental.Domain.Fleet;

namespace CarRental.Tests.Domain;

public class CarTests
{
    [Fact]
    public void Registered_car_is_available_with_given_state()
    {
        var car = TestData.Car(CarClass.Comfort, 1400m, 5_000);
        Assert.Equal(CarStatus.Available, car.Status);
        Assert.True(car.IsRentable);
        Assert.Equal(5_000, car.Mileage);
        Assert.Equal(new Money(1400m), car.DailyRate);
    }

    [Theory]
    [InlineData(0, 100)]
    [InlineData(800, -1)]
    public void Register_rejects_nonpositive_rate_and_negative_mileage(double rate, int mileage) =>
        Assert.Throws<DomainRuleException>(() => TestData.Car(rate: (decimal)rate, mileage: mileage));

    [Fact]
    public void Mileage_cannot_decrease()
    {
        var car = TestData.Car(mileage: 10_000);
        Assert.Throws<DomainRuleException>(() => car.RecordMileage(9_999));
        car.RecordMileage(10_250);
        Assert.Equal(10_250, car.Mileage);
    }

    [Fact]
    public void Car_in_maintenance_is_not_rentable()
    {
        var car = TestData.Car();
        car.SendToMaintenance();
        Assert.False(car.IsRentable);
        car.ReturnToService();
        Assert.True(car.IsRentable);
    }

    [Fact]
    public void Describe_is_overridden_and_dispatched_virtually()
    {
        CarRental.Domain.Common.Entity entity = TestData.Car();
        Assert.Contains("Skoda Fabia", entity.Describe());
    }
}
