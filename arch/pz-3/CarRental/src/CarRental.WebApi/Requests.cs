using CarRental.Domain.Common;

namespace CarRental.WebApi;

/// <summary>Тіла HTTP-запитів. Це не доменні типи: форма API може змінюватися незалежно від моделі.</summary>
public sealed record RegisterCarRequest(string? Plate, string? Model, CarClass Class, decimal DailyRate, int Mileage);
public sealed record RegisterRenterRequest(string? FullName, string? Email, DateOnly BirthDate,
    string? LicenseNumber, DateOnly LicenseValidUntil);
public sealed record CompleteRentalRequest(int EndMileage);
