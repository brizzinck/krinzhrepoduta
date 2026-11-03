using CarRental.Domain.Common;

namespace CarRental.Application.Rentals.Pricing;

/// <summary>Знижка 10% на оренду від 7 днів.</summary>
public sealed class LongRentalDiscountRule : IPricingRule
{
    public const int MinDays = 7;

    public int Order => 20;

    public Money Apply(Money current, PricingContext context) =>
        context.Period.Days >= MinDays ? current.Multiply(0.9m) : current;
}
