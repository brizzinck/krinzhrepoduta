using CarRental.Application.Rentals.Dto;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>
/// Контракт між контекстами: Rentals бачить автопарк лише через цей порт
/// і лише в тому вигляді, який йому потрібен (RentableCar).
/// </summary>
public interface IFleetGateway
{
    Task<RentableCar?> GetRentableCar(Guid carId, CancellationToken ct);

    /// <summary>Коротка довідка про автомобіль для відповідей про оренду.</summary>
    Task<CarSummaryDto?> GetCarSummary(Guid carId, CancellationToken ct);
}
