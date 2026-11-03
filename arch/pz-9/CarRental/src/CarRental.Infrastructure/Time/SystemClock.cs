using CarRental.Application.Common;

namespace CarRental.Infrastructure.Time;

/// <summary>Системний годинник. Без стану, тому живе як Singleton.</summary>
public sealed class SystemClock : IClock
{
    public DateTime UtcNow => DateTime.UtcNow;

    public DateOnly Today => DateOnly.FromDateTime(UtcNow);
}
