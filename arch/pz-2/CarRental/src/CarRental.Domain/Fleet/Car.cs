using CarRental.Domain.Common;

namespace CarRental.Domain.Fleet;

/// <summary>Корінь агрегату контексту Fleet: автомобіль автопарку, його ставка, пробіг і стан.</summary>
public sealed class Car : Entity
{
    public LicensePlate Plate { get; private set; } = null!;
    public string Model { get; private set; } = "";
    public CarClass Class { get; private set; }
    public Money DailyRate { get; private set; } = null!;
    public int Mileage { get; private set; }
    public CarStatus Status { get; private set; }

    // Версія для виявлення одночасних змін під час збереження.
    public int Version { get; private set; }

    private Car() { } // Для EF Core під час читання з БД.

    /// <summary>Фабрика: єдиний спосіб створити автомобіль із перевіркою інваріантів.</summary>
    public static Car Register(LicensePlate plate, string? model, CarClass carClass, Money dailyRate, int mileage)
    {
        ArgumentNullException.ThrowIfNull(plate);
        ArgumentNullException.ThrowIfNull(dailyRate);
        var normalizedModel = model?.Trim();
        if (string.IsNullOrEmpty(normalizedModel) || normalizedModel.Length > 60)
            throw new DomainRuleException("Модель має містити від 1 до 60 символів.");
        if (!Enum.IsDefined(carClass))
            throw new DomainRuleException("Невідомий клас автомобіля.");
        if (dailyRate.Amount <= 0)
            throw new DomainRuleException("Добова ставка має бути більшою за нуль.");
        if (mileage < 0)
            throw new DomainRuleException("Пробіг не може бути від'ємним.");

        return new Car
        {
            Plate = plate,
            Model = normalizedModel,
            Class = carClass,
            DailyRate = dailyRate,
            Mileage = mileage,
            Status = CarStatus.Available
        };
    }

    /// <summary>Чи можна зараз віддати автомобіль в оренду.</summary>
    public bool IsRentable => Status == CarStatus.Available;

    public void SendToMaintenance()
    {
        if (Status == CarStatus.Retired)
            throw new DomainRuleException("Списаний автомобіль не можна відправити на обслуговування.");
        Status = CarStatus.InMaintenance;
        Version++;
    }

    public void ReturnToService()
    {
        if (Status != CarStatus.InMaintenance)
            throw new DomainRuleException("Повернути в експлуатацію можна лише автомобіль на обслуговуванні.");
        Status = CarStatus.Available;
        Version++;
    }

    /// <summary>Фіксує новий пробіг: він не може бути меншим за поточний.</summary>
    public void RecordMileage(int newMileage)
    {
        if (newMileage < Mileage)
            throw new DomainRuleException($"Пробіг не може зменшуватися (зараз {Mileage} км, отримано {newMileage} км).");
        if (newMileage == Mileage)
            return;
        Mileage = newMileage;
        Version++;
    }

    public override string Describe() => $"{Plate} {Model} ({Class}), {Mileage} км, {Status}";
}
