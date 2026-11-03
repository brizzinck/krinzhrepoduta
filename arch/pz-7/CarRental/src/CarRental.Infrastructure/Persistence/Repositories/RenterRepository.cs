using CarRental.Application.Rentals;
using CarRental.Domain.Rentals;

namespace CarRental.Infrastructure.Persistence.Repositories;

public sealed class RenterRepository(CarRentalDbContext db) : EfRepository<Renter>(db), IRenterRepository;
