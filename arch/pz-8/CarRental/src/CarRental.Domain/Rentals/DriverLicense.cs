using CarRental.Domain.Common;

namespace CarRental.Domain.Rentals;

/// <summary>Value Object: посвідчення водія (номер і дата, до якої воно діє).</summary>
public sealed record DriverLicense
{
    public string Number { get; }
    public DateOnly ValidUntil { get; }

    public DriverLicense(string? number, DateOnly validUntil)
    {
        var normalized = number?.Trim().ToUpperInvariant();
        if (string.IsNullOrEmpty(normalized) || normalized.Length is < 5 or > 20)
            throw new DomainRuleException("Номер посвідчення водія має містити від 5 до 20 символів.");
        Number = normalized;
        ValidUntil = validUntil;
    }

    /// <summary>Чи діє посвідчення до вказаної дати включно.</summary>
    public bool IsValidThrough(DateOnly date) => ValidUntil >= date;
}
