using CarRental.Domain.Common;

namespace CarRental.Domain.Rentals;

/// <summary>
/// Value Object: період оренди [Start, End). End це день повернення, тому оренда
/// "до дати" не займає цей день і наступна оренда може починатися в день повернення.
/// </summary>
public sealed record RentalPeriod
{
    public const int MinDays = 1;
    public const int MaxDays = 30;

    public DateOnly Start { get; }
    public DateOnly End { get; }

    public RentalPeriod(DateOnly start, DateOnly end)
    {
        var days = end.DayNumber - start.DayNumber;
        if (days < MinDays)
            throw new DomainRuleException("Дата повернення має бути пізніше за дату початку.");
        if (days > MaxDays)
            throw new DomainRuleException($"Оренда не може тривати довше {MaxDays} днів.");
        Start = start;
        End = end;
    }

    public int Days => End.DayNumber - Start.DayNumber;

    public bool Overlaps(RentalPeriod other) => Start < other.End && other.Start < End;
}
