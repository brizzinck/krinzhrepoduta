namespace CarRental.Application.Common;

/// <summary>
/// Джерело поточного часу як залежність: сценарії не читають системний годинник напряму,
/// тож у тестах час можна підмінити. Реалізація (SystemClock) лежить в Infrastructure.
/// </summary>
public interface IClock
{
    DateTime UtcNow { get; }

    /// <summary>Поточна дата за UTC.</summary>
    DateOnly Today { get; }
}
