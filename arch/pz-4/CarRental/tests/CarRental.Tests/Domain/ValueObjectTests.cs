using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;

namespace CarRental.Tests.Domain;

public class ValueObjectTests
{
    [Fact]
    public void Money_rounds_to_kopecks_and_compares_by_value()
    {
        Assert.Equal(new Money(10.005m), new Money(10.01m));
        Assert.Equal("10.01 UAH", new Money(10.005m).ToString());
    }

    [Fact]
    public void Money_rejects_negative_amount() =>
        Assert.Throws<DomainRuleException>(() => new Money(-1m));

    [Theory]
    [InlineData("aa1234bb", "AA1234BB")]
    [InlineData("  KA0001AB ", "KA0001AB")]
    public void LicensePlate_is_normalized(string input, string expected) =>
        Assert.Equal(expected, new LicensePlate(input).Value);

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData("AA123BB")]
    [InlineData("12AAAA34")]
    [InlineData("АА1234ВВ")] // кирилиця не допускається
    public void LicensePlate_rejects_bad_format(string? input) =>
        Assert.Throws<DomainRuleException>(() => new LicensePlate(input));

    [Fact]
    public void LicensePlate_equality_is_by_value() =>
        Assert.Equal(new LicensePlate("aa1234bb"), new LicensePlate("AA1234BB"));

    [Fact]
    public void RentalPeriod_counts_days_and_detects_overlap()
    {
        var a = new RentalPeriod(new DateOnly(2026, 12, 1), new DateOnly(2026, 12, 4));
        Assert.Equal(3, a.Days);
        Assert.True(a.Overlaps(new RentalPeriod(new DateOnly(2026, 12, 3), new DateOnly(2026, 12, 6))));
        // Повернення й наступна видача в один день не є перетином.
        Assert.False(a.Overlaps(new RentalPeriod(new DateOnly(2026, 12, 4), new DateOnly(2026, 12, 6))));
    }

    [Theory]
    [InlineData(0)]
    [InlineData(-2)]
    [InlineData(31)]
    public void RentalPeriod_rejects_length_outside_1_to_30_days(int days) =>
        Assert.Throws<DomainRuleException>(() => new RentalPeriod(new DateOnly(2026, 12, 1), new DateOnly(2026, 12, 1).AddDays(days)));

    [Fact]
    public void RentalPeriod_accepts_boundaries()
    {
        var start = new DateOnly(2026, 12, 1);
        Assert.Equal(1, new RentalPeriod(start, start.AddDays(1)).Days);
        Assert.Equal(30, new RentalPeriod(start, start.AddDays(30)).Days);
    }
}
