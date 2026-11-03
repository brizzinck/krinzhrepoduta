namespace CarRental.Application.Rentals.Events;

/// <summary>Факт: оренду завершено, автомобіль повернуто з вказаним пробігом.</summary>
public sealed record RentalCompleted(Guid RentalId, Guid CarId, int EndMileage);
