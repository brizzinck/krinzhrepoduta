using CarRental.Application.Rentals.Dto;

namespace CarRental.Application.Rentals.UseCases;

/// <summary>Читання оренд. Контролер не знає, чи є між ним і БД кеш.</summary>
public interface IRentalQueryService
{
    Task<RentalDetailsDto> Get(Guid id, CancellationToken ct = default);
    Task<IReadOnlyList<RentalListItemDto>> List(Guid? renterId, CancellationToken ct = default);
}
