namespace CarRental.Application.Rentals.Events;

/// <summary>Факт: оренду відкрито. Лише дані, потрібні споживачам, без логіки.</summary>
public sealed record RentalOpened(Guid RentalId, Guid CarId, Guid RenterId, DateOnly StartDate, DateOnly EndDate, decimal Total);
