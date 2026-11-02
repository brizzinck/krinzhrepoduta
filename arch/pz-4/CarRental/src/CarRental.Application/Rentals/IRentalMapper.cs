using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>Перетворення оренди на DTO як залежність (а не статичний клас), яку можна підмінити.</summary>
public interface IRentalMapper
{
    RentalDto ToDto(Rental rental);
}
