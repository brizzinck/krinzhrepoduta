namespace CarRental.Application.Rentals.Events;

/// <summary>Факт: оренду скасовано до початку.</summary>
public sealed record RentalCancelled(Guid RentalId, Guid CarId);
