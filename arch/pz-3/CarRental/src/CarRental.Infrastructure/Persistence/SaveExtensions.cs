using CarRental.Application.Common;
using Microsoft.EntityFrameworkCore;

namespace CarRental.Infrastructure.Persistence;

internal static class SaveExtensions
{
    /// <summary>Зберігає зміни; специфічний виняток EF приховуємо за типом Application.</summary>
    public static async Task SaveOrConflict(this DbContext db, CancellationToken ct)
    {
        try { await db.SaveChangesAsync(ct); }
        catch (DbUpdateConcurrencyException) { throw new ConcurrentChangeException(); }
    }
}
