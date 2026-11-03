using CarRental.Domain.Common;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals.Pricing;

/// <summary>Розрахунок вартості оренди; сценарії залежать від цієї абстракції.</summary>
public interface IPricingService
{
    Money Calculate(RentableCar car, RentalPeriod period);
}
