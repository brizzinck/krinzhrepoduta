using CarRental.Domain.Common;

namespace CarRental.Application.Rentals.Pricing;

/// <summary>Коефіцієнт тарифу за класом автомобіля. Значення зберігаються в таблиці, а не в розгалуженні.</summary>
public sealed class ClassMultiplierRule : IPricingRule
{
    private static readonly IReadOnlyDictionary<CarClass, decimal> Multipliers = new Dictionary<CarClass, decimal>
    {
        [CarClass.Economy] = 1.0m,
        [CarClass.Comfort] = 1.25m,
        [CarClass.Premium] = 1.6m
    };

    public int Order => 10;

    public Money Apply(Money current, PricingContext context) =>
        Multipliers.TryGetValue(context.Car.Class, out var factor)
            ? current.Multiply(factor)
            : throw new DomainRuleException("Невідомий клас автомобіля.");
}
