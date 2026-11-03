using AutoMapper;
using CarRental.Application.Fleet.Dto;
using CarRental.Application.Rentals.Dto;

namespace CarRental.WebApi.Mapping;

/// <summary>Мапінг між моделями API та прикладного шару.</summary>
public sealed class ApiProfile : Profile
{
    public ApiProfile()
    {
        CreateMap<OpenRentalRequest, OpenRentalCommand>().ReverseMap();
        CreateMap<RegisterRenterRequest, RegisterRenterCommand>().ReverseMap();
        CreateMap<RegisterCarRequest, RegisterCarCommand>().ReverseMap();
        CreateMap<CarDto, CarResponse>()
            .ForCtorParam(nameof(CarResponse.Title), o => o.MapFrom(c => $"{c.Model} ({c.Plate})"));
    }
}
