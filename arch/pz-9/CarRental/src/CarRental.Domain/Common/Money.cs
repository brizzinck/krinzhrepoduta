using System.Globalization;

namespace CarRental.Domain.Common;

/// <summary>Value Object: сума в гривнях, не менша за нуль, округлена до копійок.</summary>
public sealed record Money
{
    public decimal Amount { get; }
    public string Currency => "UAH";

    public Money(decimal amount)
    {
        if (amount < 0)
            throw new DomainRuleException("Сума не може бути від'ємною.");
        Amount = Math.Round(amount, 2, MidpointRounding.AwayFromZero);
    }

    public static Money Zero => new(0m);

    public Money Multiply(decimal factor) => new(Amount * factor);

    public override string ToString() => $"{Amount.ToString("0.00", CultureInfo.InvariantCulture)} {Currency}";
}
