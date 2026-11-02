using CarRental.Domain.Common;

namespace CarRental.Domain.Rentals;

/// <summary>
/// Модель автомобіля в контексті Rentals: лише те, що потрібно для оренди.
/// Це інша модель того самого поняття, ніж Car у контексті Fleet.
/// </summary>
public sealed record RentableCar(Guid Id, Money DailyRate, CarClass Class, int Mileage, bool IsRentable);
