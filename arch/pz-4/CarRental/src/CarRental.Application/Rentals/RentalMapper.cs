using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>Реалізація мапера: без стану, тому реєструється як Transient.</summary>
public sealed class RentalMapper : IRentalMapper
{
    public RentalDto ToDto(Rental r) => new(r.Id, r.CarId, r.RenterId, r.Period.Start, r.Period.End,
        r.Period.Days, r.DailyRate.Amount, r.Total.Amount, r.Status.ToString(), r.StartMileage, r.EndMileage);
}
