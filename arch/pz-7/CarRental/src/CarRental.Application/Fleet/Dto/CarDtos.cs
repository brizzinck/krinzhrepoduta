namespace CarRental.Application.Fleet.Dto;

/// <summary>Дані автомобіля для прикладного шару, без доменної поведінки.</summary>
public sealed record CarDto(Guid Id, string Plate, string Model, string Class, decimal DailyRate, int Mileage, string Status);

/// <summary>Вхід сценарію реєстрації. Клас передається рядком: доменний enum не виходить за межі Application.</summary>
public sealed record RegisterCarCommand(string? Plate, string? Model, string? Class, decimal DailyRate, int Mileage);
