using CarRental.Domain.Common;

namespace CarRental.Domain.Rentals;

/// <summary>Корінь агрегату контексту Rentals: оренда одного автомобіля одним орендарем на період.</summary>
public sealed class Rental : Entity
{
    // На інші агрегати посилаємося лише за Id.
    public Guid CarId { get; private set; }
    public Guid RenterId { get; private set; }
    public RentalPeriod Period { get; private set; } = null!;
    public Money DailyRate { get; private set; } = null!;
    public Money Total { get; private set; } = null!;
    public RentalStatus Status { get; private set; }
    public int StartMileage { get; private set; }
    public int? EndMileage { get; private set; }
    public int Version { get; private set; }

    private Rental() { } // Для EF Core.

    /// <summary>
    /// Відкриває оренду після перевірки всіх правил.
    /// busyPeriods: періоди активних оренд цього автомобіля, які надає Application.
    /// </summary>
    public static Rental Open(RentableCar car, Renter renter, RentalPeriod period, Money total,
        DateOnly today, IEnumerable<RentalPeriod> busyPeriods)
    {
        ArgumentNullException.ThrowIfNull(car);
        ArgumentNullException.ThrowIfNull(renter);
        ArgumentNullException.ThrowIfNull(period);
        ArgumentNullException.ThrowIfNull(total);
        if (!car.IsRentable)
            throw new DomainRuleException("Автомобіль зараз недоступний для оренди.");
        if (period.Start < today)
            throw new DomainRuleException("Оренда не може починатися в минулому.");
        renter.EnsureCanRent(period);
        if (total.Amount <= 0)
            throw new DomainRuleException("Вартість оренди має бути більшою за нуль.");
        if (busyPeriods.Any(p => p.Overlaps(period)))
            throw new DomainConflictException("На ці дати автомобіль уже орендований.");

        return new Rental
        {
            CarId = car.Id,
            RenterId = renter.Id,
            Period = period,
            DailyRate = car.DailyRate,
            Total = total,
            Status = RentalStatus.Active,
            StartMileage = car.Mileage
        };
    }

    public void Complete(int endMileage)
    {
        if (Status != RentalStatus.Active)
            throw new DomainRuleException("Завершити можна лише активну оренду.");
        if (endMileage < StartMileage)
            throw new DomainRuleException($"Кінцевий пробіг не може бути меншим за початковий ({StartMileage} км).");
        EndMileage = endMileage;
        Status = RentalStatus.Completed;
        Version++;
    }

    public void Cancel(DateOnly today)
    {
        if (Status != RentalStatus.Active)
            throw new DomainRuleException("Скасувати можна лише активну оренду.");
        if (today >= Period.Start)
            throw new DomainRuleException("Оренду можна скасувати лише до дати початку.");
        Status = RentalStatus.Cancelled;
        Version++;
    }

    public override string Describe() => $"Оренда {Period.Start:yyyy-MM-dd}..{Period.End:yyyy-MM-dd}, {Total}, {Status}";
}
