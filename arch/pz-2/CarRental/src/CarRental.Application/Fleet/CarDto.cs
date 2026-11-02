namespace CarRental.Application.Fleet;

/// <summary>Дані автомобіля для відповіді API без доменної поведінки.</summary>
public sealed record CarDto(Guid Id, string Plate, string Model, string Class, decimal DailyRate, int Mileage, string Status);
