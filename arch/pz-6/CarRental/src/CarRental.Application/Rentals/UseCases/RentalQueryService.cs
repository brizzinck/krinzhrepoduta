using AutoMapper;
using CarRental.Application.Common;
using CarRental.Application.Mapping;
using CarRental.Application.Rentals.Dto;

namespace CarRental.Application.Rentals.UseCases;

/// <summary>Читання оренд. Методів запису цей клас не бачить, автопарк читає лише через IFleetGateway.</summary>
public sealed class RentalQueryService(IRentalReader rentals, IRenterReader renters, IFleetGateway fleet, IMapper mapper)
{
    public async Task<RentalDetailsDto> Get(Guid id, CancellationToken ct = default)
    {
        var rental = await rentals.GetByIdAsync(id, ct) ?? throw new NotFoundException("Оренду");
        var renter = await renters.GetByIdAsync(rental.RenterId, ct) ?? throw new NotFoundException("Орендаря");
        var car = await fleet.GetCarSummary(rental.CarId, ct) ?? throw new NotFoundException("Автомобіль");
        return mapper.Map<RentalDetailsDto>(new RentalDetailsSource(rental, renter, car));
    }

    public async Task<IReadOnlyList<RentalListItemDto>> List(Guid? renterId, CancellationToken ct = default) =>
        mapper.Map<IReadOnlyList<RentalListItemDto>>(await rentals.ListByRenterAsync(renterId, ct));
}
