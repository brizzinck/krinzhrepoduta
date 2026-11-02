using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

public interface IRenterStore
{
    Task<Renter?> Find(Guid id, CancellationToken ct);
    Task Add(Renter renter, CancellationToken ct);
    Task Save(CancellationToken ct);
}
