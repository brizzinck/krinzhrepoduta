using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence;

/// <summary>EF Core: відображення доменних об'єктів на таблиці SQLite. Бізнес-правил тут немає.</summary>
public sealed class CarRentalDbContext(DbContextOptions<CarRentalDbContext> options) : DbContext(options)
{
    public DbSet<Car> Cars => Set<Car>();
    public DbSet<Renter> Renters => Set<Renter>();
    public DbSet<Rental> Rentals => Set<Rental>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        var car = modelBuilder.Entity<Car>();
        car.HasKey(c => c.Id);
        car.Property(c => c.Id).ValueGeneratedNever();
        // Value Object зберігаємо як текст; унікальність номера захищає й сама БД.
        car.Property(c => c.Plate).HasConversion(p => p.Value, v => new LicensePlate(v)).HasMaxLength(8);
        car.HasIndex(c => c.Plate).IsUnique();
        car.Property(c => c.DailyRate).HasConversion(m => m.Amount, v => new Money(v));
        car.Property(c => c.Class).HasConversion<string>();
        car.Property(c => c.Status).HasConversion<string>();
        car.Property(c => c.Version).IsConcurrencyToken();

        var renter = modelBuilder.Entity<Renter>();
        renter.HasKey(r => r.Id);
        renter.Property(r => r.Id).ValueGeneratedNever();
        // Посвідчення зберігається як складений Value Object у колонках таблиці Renters.
        // Властивості VO без сетерів мапимо явно; EF створює об'єкт через конструктор.
        renter.ComplexProperty(r => r.License, b =>
        {
            b.Property(l => l.Number).HasMaxLength(20);
            b.Property(l => l.ValidUntil);
        });

        var rental = modelBuilder.Entity<Rental>();
        rental.HasKey(r => r.Id);
        rental.Property(r => r.Id).ValueGeneratedNever();
        rental.ComplexProperty(r => r.Period, b =>
        {
            b.Property(p => p.Start);
            b.Property(p => p.End);
        });
        rental.Property(r => r.DailyRate).HasConversion(m => m.Amount, v => new Money(v));
        rental.Property(r => r.Total).HasConversion(m => m.Amount, v => new Money(v));
        rental.Property(r => r.Status).HasConversion<string>();
        rental.Property(r => r.Version).IsConcurrencyToken();
        // Агрегати посилаються один на одного лише за Id, без навігацій.
        rental.HasIndex(r => r.CarId);
        rental.HasIndex(r => r.RenterId);
    }
}
