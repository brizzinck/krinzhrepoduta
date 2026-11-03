using CarRental.Application.Common;
using CarRental.Application.Common.Persistence;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Rentals;

/// <summary>Сценарії реєстрації й перегляду орендарів.</summary>
public sealed class RenterService(IUnitOfWork uow, IClock clock)
{
    public async Task<RenterDto> Register(string? fullName, string? email, DateOnly birthDate,
        string? licenseNumber, DateOnly licenseValidUntil, CancellationToken ct = default)
    {
        var renter = Renter.Register(fullName, email, birthDate, new DriverLicense(licenseNumber, licenseValidUntil), clock.Today);
        await uow.Renters.AddAsync(renter, ct);
        await uow.SaveChangesAsync(ct);
        return ToDto(renter);
    }

    public async Task<RenterDto> Get(Guid id, CancellationToken ct = default) =>
        ToDto(await uow.Renters.GetByIdAsync(id, ct) ?? throw new NotFoundException("Орендаря"));

    private static RenterDto ToDto(Renter r) =>
        new(r.Id, r.FullName, r.Email, r.BirthDate, r.License.Number, r.License.ValidUntil);
}
