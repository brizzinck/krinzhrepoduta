using AutoMapper;
using CarRental.Application.Rentals.Dto;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Mapping;

public sealed class RenterProfile : Profile
{
    public RenterProfile()
    {
        CreateMap<Renter, RenterDto>()
            .ForCtorParam(nameof(RenterDto.LicenseNumber), o => o.MapFrom(r => r.License.Number))
            .ForCtorParam(nameof(RenterDto.LicenseValidUntil), o => o.MapFrom(r => r.License.ValidUntil));
        CreateMap<Renter, RenterRegisteredDto>();
        CreateMap<Renter, RenterSummaryDto>();
    }
}
