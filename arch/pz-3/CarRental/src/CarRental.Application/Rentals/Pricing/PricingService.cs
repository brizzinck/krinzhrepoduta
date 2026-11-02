using CarRental.Domain.Common;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals.Pricing;

/// <summary>Базова вартість (ставка x дні) із послідовним застосуванням усіх зареєстрованих правил.</summary>
public sealed class PricingService(IEnumerable<IPricingRule> rules) : IPricingService
{
    private readonly IPricingRule[] _rules = rules.OrderBy(r => r.Order).ToArray();

    public Money Calculate(RentableCar car, RentalPeriod period)
    {
        var context = new PricingContext(car, period);
        var total = car.DailyRate.Multiply(period.Days);
        foreach (var rule in _rules)
            total = rule.Apply(total, context);
        return total;
    }
}
