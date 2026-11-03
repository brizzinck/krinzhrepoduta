using AutoMapper;
using CarRental.Application.Fleet.Dto;
using CarRental.Application.Rentals.Dto;
using CarRental.Domain.Fleet;

namespace CarRental.Application.Mapping;

public sealed class CarProfile : Profile
{
    public CarProfile()
    {
        CreateMap<LicensePlate, string>().ConvertUsing(p => p.Value);
        CreateMap<Car, CarDto>()
            .ForCtorParam(nameof(CarDto.DailyRate), o => o.MapFrom(c => c.DailyRate.Amount));
        CreateMap<Car, CarSummaryDto>();
    }
}
