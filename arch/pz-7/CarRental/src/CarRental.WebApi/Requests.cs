namespace CarRental.WebApi;

/// <summary>Тіла HTTP-запитів і відповіді API. Форма API може змінюватися незалежно від моделі.</summary>
public sealed record RegisterCarRequest(string? Plate, string? Model, string? Class, decimal DailyRate, int Mileage);
public sealed record RegisterRenterRequest(string? FullName, string? Email, DateOnly BirthDate,
    string? LicenseNumber, DateOnly LicenseValidUntil);
public sealed record OpenRentalRequest(Guid CarId, Guid RenterId, DateOnly Start, DateOnly End);
public sealed record CompleteRentalRequest(int EndMileage);

/// <summary>Третій рівень моделі автомобіля: Car, далі CarDto, далі ця відповідь із готовою назвою.</summary>
public sealed record CarResponse(Guid Id, string Title, string Plate, string Class, decimal DailyRate, int Mileage, string Status);
