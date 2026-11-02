namespace CarRental.Application.Rentals;

/// <summary>Одна форма даних оренди для всіх відповідей (перша версія).</summary>
public sealed record RentalDto(Guid Id, Guid CarId, Guid RenterId, DateOnly Start, DateOnly End, int Days,
    decimal DailyRate, decimal Total, string Status, int StartMileage, int? EndMileage);

public sealed record RenterDto(Guid Id, string FullName, string Email, DateOnly BirthDate,
    string LicenseNumber, DateOnly LicenseValidUntil);

public sealed record OpenRentalCommand(Guid CarId, Guid RenterId, DateOnly Start, DateOnly End);
