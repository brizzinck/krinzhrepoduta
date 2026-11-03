using AutoMapper;
using CarRental.Application.Rentals.Dto;
using CarRental.Domain.Common;
using CarRental.Domain.Rentals;

namespace CarRental.Application.Mapping;

/// <summary>Джерело для деталей оренди: дані з трьох місць, зібрані сценарієм читання.</summary>
public sealed record RentalDetailsSource(Rental Rental, Renter Renter, CarSummaryDto Car);

public sealed class RentalProfile : Profile
{
    public RentalProfile()
    {
        CreateMap<Money, MoneyDto>().ConvertUsing<MoneyConverter>();

        // Зворотний напрям іде через конструктор: інваріанти періоду перевіряються й тут.
        CreateMap<RentalPeriod, PeriodDto>()
            .ReverseMap()
            .ConstructUsing(d => new RentalPeriod(d.Start, d.End));

        CreateMap<Rental, RentalCreatedDto>();
        CreateMap<Rental, RentalListItemDto>();
        CreateMap<Rental, RentalClosedDto>();

        CreateMap<RentalDetailsSource, RentalDetailsDto>()
            .ForCtorParam(nameof(RentalDetailsDto.Id), o => o.MapFrom(s => s.Rental.Id))
            .ForCtorParam(nameof(RentalDetailsDto.Period), o => o.MapFrom(s => s.Rental.Period))
            .ForCtorParam(nameof(RentalDetailsDto.DailyRate), o => o.MapFrom(s => s.Rental.DailyRate))
            .ForCtorParam(nameof(RentalDetailsDto.Total), o => o.MapFrom(s => s.Rental.Total))
            .ForCtorParam(nameof(RentalDetailsDto.Status), o => o.MapFrom(s => s.Rental.Status))
            .ForCtorParam(nameof(RentalDetailsDto.StartMileage), o => o.MapFrom(s => s.Rental.StartMileage))
            .ForCtorParam(nameof(RentalDetailsDto.EndMileage), o => o.MapFrom(s => s.Rental.EndMileage));
    }
}
