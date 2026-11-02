using System.Text.RegularExpressions;
using CarRental.Domain.Common;

namespace CarRental.Domain.Fleet;

/// <summary>Value Object: державний номер у форматі AA1234BB (латиниця), нормалізований до верхнього регістру.</summary>
public sealed partial record LicensePlate
{
    public string Value { get; }

    public LicensePlate(string? value)
    {
        var normalized = value?.Trim().ToUpperInvariant();
        if (string.IsNullOrEmpty(normalized) || !PlateFormat().IsMatch(normalized))
            throw new DomainRuleException("Номер автомобіля має формат AA1234BB (дві літери, чотири цифри, дві літери).");
        Value = normalized;
    }

    [GeneratedRegex("^[A-Z]{2}[0-9]{4}[A-Z]{2}$")]
    private static partial Regex PlateFormat();

    public override string ToString() => Value;
}
