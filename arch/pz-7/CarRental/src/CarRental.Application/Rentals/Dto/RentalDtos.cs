namespace CarRental.Application.Rentals.Dto;

// Кожна операція має власну форму даних: лише поля, потрібні її клієнту.

public sealed record MoneyDto(decimal Amount, string Currency, string Formatted);
public sealed record PeriodDto(DateOnly Start, DateOnly End, int Days);
public sealed record CarSummaryDto(Guid Id, string Plate, string Model);
public sealed record RenterSummaryDto(Guid Id, string FullName, string Email);

/// <summary>Відповідь на відкриття оренди.</summary>
public sealed record RentalCreatedDto(Guid Id, PeriodDto Period, MoneyDto Total, string Status);

/// <summary>Рядок списку: плоска форма, поля Period і Total розгорнуто.</summary>
public sealed record RentalListItemDto(Guid Id, Guid CarId, DateOnly PeriodStart, DateOnly PeriodEnd, decimal TotalAmount, string Status);

/// <summary>Деталі оренди із вкладеними даними автомобіля й орендаря.</summary>
public sealed record RentalDetailsDto(Guid Id, CarSummaryDto Car, RenterSummaryDto Renter, PeriodDto Period,
    MoneyDto DailyRate, MoneyDto Total, string Status, int StartMileage, int? EndMileage);

/// <summary>Відповідь на завершення чи скасування.</summary>
public sealed record RentalClosedDto(Guid Id, string Status, int? EndMileage);

public sealed record RenterDto(Guid Id, string FullName, string Email, DateOnly BirthDate,
    string LicenseNumber, DateOnly LicenseValidUntil);

/// <summary>Відповідь на реєстрацію: без дати народження й посвідчення.</summary>
public sealed record RenterRegisteredDto(Guid Id, string FullName, string Email);

public sealed record OpenRentalCommand(Guid CarId, Guid RenterId, DateOnly Start, DateOnly End);

public sealed record RegisterRenterCommand(string? FullName, string? Email, DateOnly BirthDate,
    string? LicenseNumber, DateOnly LicenseValidUntil);
