using AutoMapper;
using CarRental.Application.Rentals.Dto;
using CarRental.Domain.Common;

namespace CarRental.Application.Mapping;

/// <summary>Власний конвертер: Value Object Money стає сумою, валютою й готовим рядком.</summary>
public sealed class MoneyConverter : ITypeConverter<Money, MoneyDto>
{
    public MoneyDto Convert(Money source, MoneyDto destination, ResolutionContext context) =>
        new(source.Amount, source.Currency, source.ToString());
}
