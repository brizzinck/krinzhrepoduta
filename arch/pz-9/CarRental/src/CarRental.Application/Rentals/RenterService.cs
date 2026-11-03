using AutoMapper;
using CarRental.Application.Common;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Rentals.Dto;
using CarRental.Domain.Rentals;
using FluentValidation;

namespace CarRental.Application.Rentals;

/// <summary>Сценарії реєстрації й перегляду орендарів.</summary>
public sealed class RenterService(IUnitOfWork uow, IClock clock, IMapper mapper, IValidator<RegisterRenterCommand> validator)
{
    public async Task<RenterRegisteredDto> Register(RegisterRenterCommand cmd, CancellationToken ct = default)
    {
        await validator.ValidateAndThrowAsync(cmd, ct);
        var renter = Renter.Register(cmd.FullName, cmd.Email, cmd.BirthDate,
            new DriverLicense(cmd.LicenseNumber, cmd.LicenseValidUntil), clock.Today);
        await uow.Renters.AddAsync(renter, ct);
        await uow.SaveChangesAsync(ct);
        return mapper.Map<RenterRegisteredDto>(renter);
    }

    public async Task<RenterDto> Get(Guid id, CancellationToken ct = default) =>
        mapper.Map<RenterDto>(await uow.Renters.GetByIdAsync(id, ct) ?? throw new NotFoundException("Орендаря"));
}
