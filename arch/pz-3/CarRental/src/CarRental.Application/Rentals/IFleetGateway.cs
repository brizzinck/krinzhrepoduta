using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>
/// Контракт між контекстами: Rentals бачить автопарк лише через цей порт
/// і лише в тому вигляді, який йому потрібен (RentableCar).
/// </summary>
public interface IFleetGateway
{
    Task<RentableCar?> GetRentableCar(Guid carId, CancellationToken ct);
}
