using CarRental.Application.Rentals.Pricing;
using CarRental.Domain.Common;
using CarRental.Domain.Rentals;

namespace CarRental.Tests.Pricing;

public class PricingTests
{
    private static PricingService Standard() => new([new ClassMultiplierRule(), new LongRentalDiscountRule()]);

    private static RentalPeriod Days(int days, DateOnly? start = null) =>
        new(start ?? new DateOnly(2026, 12, 7), (start ?? new DateOnly(2026, 12, 7)).AddDays(days)); // 2026-12-07 понеділок

    [Theory]
    [InlineData(CarClass.Economy, 1000, 1000)]
    [InlineData(CarClass.Comfort, 1000, 1250)]
    [InlineData(CarClass.Premium, 1000, 1600)]
    public void Class_multiplier_rule_scales_amount_by_class(CarClass carClass, decimal start, decimal expected)
    {
        var context = new PricingContext(TestData.RentableCar(carClass), Days(1));
        Assert.Equal(new Money(expected), new ClassMultiplierRule().Apply(new Money(start), context));
    }

    [Theory]
    [InlineData(6, 1000, 1000)]
    [InlineData(7, 1000, 900)]
    [InlineData(30, 1000, 900)]
    public void Long_rental_discount_starts_from_seven_days(int days, decimal start, decimal expected)
    {
        var context = new PricingContext(TestData.RentableCar(), Days(days));
        Assert.Equal(new Money(expected), new LongRentalDiscountRule().Apply(new Money(start), context));
    }

    [Fact]
    public void Service_applies_rules_by_order_regardless_of_registration_order()
    {
        var car = TestData.RentableCar(CarClass.Premium, 2500m);
        var forward = new PricingService([new ClassMultiplierRule(), new LongRentalDiscountRule()]);
        var reversed = new PricingService([new LongRentalDiscountRule(), new ClassMultiplierRule()]);
        Assert.Equal(forward.Calculate(car, Days(7)), reversed.Calculate(car, Days(7)));
        Assert.Equal(new Money(25_200m), forward.Calculate(car, Days(7)));
    }

    /// <summary>Тариф ПЗ 2 (switch у RentalService): еталон для перевірки, що рефакторинг не змінив ціни.</summary>
    private static Money LegacyTotal(RentableCar car, int days)
    {
        var multiplier = car.Class switch { CarClass.Economy => 1.0m, CarClass.Comfort => 1.25m, _ => 1.6m };
        var total = car.DailyRate.Multiply(multiplier * days);
        return days >= 7 ? total.Multiply(0.9m) : total;
    }

    [Fact]
    public void Refactored_pricing_equals_legacy_formula_for_all_classes_rates_and_durations()
    {
        var service = Standard();
        var checkedCases = 0;
        foreach (var carClass in Enum.GetValues<CarClass>())
        foreach (var rate in new[] { 99.99m, 333.33m, 800m, 1234.56m, 2500.5m })
        foreach (var days in Enumerable.Range(1, 30))
        {
            var car = TestData.RentableCar(carClass, rate);
            Assert.Equal(LegacyTotal(car, days), service.Calculate(car, Days(days)));
            checkedCases++;
        }
        Assert.Equal(3 * 5 * 30, checkedCases);
    }

    // --- OCP: нове правило додається без змін у наявних класах ---

    [Fact]
    public void Weekend_surcharge_rule_extends_pricing_without_touching_existing_rules()
    {
        var car = TestData.RentableCar(CarClass.Economy, 1000m);
        var weekdays = Days(3, new DateOnly(2026, 12, 7));        // пн-ср
        var withWeekend = Days(3, new DateOnly(2026, 12, 11));    // пт-нд (є субота)
        var extended = new PricingService([new ClassMultiplierRule(), new LongRentalDiscountRule(), new WeekendSurchargeRule()]);

        Assert.Equal(Standard().Calculate(car, weekdays), extended.Calculate(car, weekdays));
        Assert.Equal(new Money(3000m), Standard().Calculate(car, withWeekend));
        Assert.Equal(new Money(3300m), extended.Calculate(car, withWeekend));
    }

    private sealed class FixedFeeRule : IPricingRule
    {
        public int Order => 100;
        public Money Apply(Money current, PricingContext context) => new(current.Amount + 150m);
    }

    [Fact]
    public void Rule_defined_outside_the_pricing_namespace_is_picked_up_by_the_service()
    {
        var car = TestData.RentableCar(CarClass.Economy, 1000m);
        var service = new PricingService([new ClassMultiplierRule(), new FixedFeeRule()]);
        Assert.Equal(new Money(2150m), service.Calculate(car, Days(2)));
    }
}
