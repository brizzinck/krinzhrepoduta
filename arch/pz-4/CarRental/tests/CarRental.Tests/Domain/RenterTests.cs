using CarRental.Domain.Common;
using CarRental.Domain.Rentals;

namespace CarRental.Tests.Domain;

public class RenterTests
{
    [Fact]
    public void Age_is_counted_in_full_years_on_given_date()
    {
        var renter = TestData.Renter(birth: new DateOnly(2000, 12, 31));
        Assert.Equal(25, renter.AgeOn(new DateOnly(2026, 12, 30)));
        Assert.Equal(26, renter.AgeOn(new DateOnly(2026, 12, 31)));
    }

    [Fact]
    public void Renter_younger_than_21_on_start_date_cannot_rent()
    {
        // 21 рік виповниться 2026-12-10, оренда починається раніше.
        var renter = TestData.Renter(birth: new DateOnly(2005, 12, 10));
        var period = new RentalPeriod(new DateOnly(2026, 12, 9), new DateOnly(2026, 12, 12));
        Assert.Throws<DomainRuleException>(() => renter.EnsureCanRent(period));
    }

    [Fact]
    public void Renter_turning_21_on_start_date_can_rent()
    {
        var renter = TestData.Renter(birth: new DateOnly(2005, 12, 10));
        renter.EnsureCanRent(new RentalPeriod(new DateOnly(2026, 12, 10), new DateOnly(2026, 12, 12)));
    }

    [Fact]
    public void License_must_be_valid_until_end_of_rental()
    {
        var renter = TestData.Renter(licenseUntil: new DateOnly(2026, 12, 5));
        Assert.Throws<DomainRuleException>(() =>
            renter.EnsureCanRent(new RentalPeriod(new DateOnly(2026, 12, 3), new DateOnly(2026, 12, 7))));
        renter.EnsureCanRent(new RentalPeriod(new DateOnly(2026, 12, 3), new DateOnly(2026, 12, 5)));
    }

    [Theory]
    [InlineData("", "a@b.c")]
    [InlineData("Іван", "no-at-sign")]
    public void Register_rejects_bad_name_or_email(string name, string email) =>
        Assert.Throws<DomainRuleException>(() => Renter.Register(name, email, new DateOnly(1990, 1, 1),
            new DriverLicense("ABC123456", new DateOnly(2035, 1, 1)), TestData.Today));
}
