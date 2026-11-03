using CarRental.Domain.Common;

namespace CarRental.Domain.Rentals;

/// <summary>Орендар у контексті Rentals: людина з посвідченням водія, яка має право брати авто.</summary>
public sealed class Renter : Entity
{
    public const int MinAge = 21;

    public string FullName { get; private set; } = "";
    public string Email { get; private set; } = "";
    public DateOnly BirthDate { get; private set; }
    public DriverLicense License { get; private set; } = null!;

    private Renter() { } // Для EF Core.

    public static Renter Register(string? fullName, string? email, DateOnly birthDate, DriverLicense license, DateOnly today)
    {
        ArgumentNullException.ThrowIfNull(license);
        var name = fullName?.Trim();
        if (string.IsNullOrEmpty(name) || name.Length > 120)
            throw new DomainRuleException("ПІБ має містити від 1 до 120 символів.");
        var mail = email?.Trim();
        if (string.IsNullOrEmpty(mail) || mail.Length > 120 || !mail.Contains('@') || mail.StartsWith('@') || mail.EndsWith('@'))
            throw new DomainRuleException("Електронна адреса має некоректний формат.");
        if (birthDate >= today || birthDate.Year < today.Year - 120)
            throw new DomainRuleException("Дата народження некоректна.");

        return new Renter { FullName = name, Email = mail, BirthDate = birthDate, License = license };
    }

    /// <summary>Повних років на вказану дату.</summary>
    public int AgeOn(DateOnly date)
    {
        var age = date.Year - BirthDate.Year;
        if (date < BirthDate.AddYears(age))
            age--;
        return age;
    }

    /// <summary>Перевіряє, що орендар може брати авто на цей період: вік на дату початку й строк дії посвідчення.</summary>
    public void EnsureCanRent(RentalPeriod period)
    {
        if (AgeOn(period.Start) < MinAge)
            throw new DomainRuleException($"Мінімальний вік орендаря — {MinAge} рік на дату початку оренди.");
        if (!License.IsValidThrough(period.End))
            throw new DomainRuleException("Посвідчення водія не діє до кінця оренди.");
    }

    public override string Describe() => $"{FullName} <{Email}>";
}
