using CarRental.Domain.Common;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals.Pricing;

/// <summary>Дані, на яких правила ціни приймають рішення.</summary>
public sealed record PricingContext(RentableCar Car, RentalPeriod Period);

/// <summary>
/// Одне правило тарифу. Нове правило додається новим класом і реєстрацією,
/// без змін у PricingService та в інших правилах (Open/Closed).
/// </summary>
public interface IPricingRule
{
    /// <summary>Порядок застосування: правила виконуються за зростанням.</summary>
    int Order { get; }

    /// <summary>Приймає поточну суму й повертає змінену.</summary>
    Money Apply(Money current, PricingContext context);
}
