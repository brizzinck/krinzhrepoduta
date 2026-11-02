using CarRental.Application.Common;

namespace CarRental.Application.Rentals.UseCases;

/// <summary>Читання оренд. Залежить лише від IRentalReader: методів запису цей клас не бачить.</summary>
public sealed class RentalQueryService(IRentalReader rentals, IRentalMapper mapper)
{
    public async Task<RentalDto> Get(Guid id, CancellationToken ct = default) =>
        mapper.ToDto(await rentals.Find(id, ct) ?? throw new NotFoundException("Оренду"));

    public async Task<IReadOnlyList<RentalDto>> List(Guid? renterId, CancellationToken ct = default) =>
        (await rentals.List(renterId, ct)).Select(mapper.ToDto).ToArray();
}
