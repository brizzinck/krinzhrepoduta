using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>Перетворення Rental на DTO; окремо від сценаріїв, щоб вони не знали форми відповіді.</summary>
public static class RentalMapper
{
    public static RentalDto ToDto(Rental r) => new(r.Id, r.CarId, r.RenterId, r.Period.Start, r.Period.End,
        r.Period.Days, r.DailyRate.Amount, r.Total.Amount, r.Status.ToString(), r.StartMileage, r.EndMileage);
}
