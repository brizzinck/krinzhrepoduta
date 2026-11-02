using CarRental.Application.Common.Audit;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Events;
using CarRental.Application.Rentals.Pricing;
using CarRental.Application.Rentals.UseCases;
using CarRental.Infrastructure.Audit;

namespace CarRental.Tests.Design;

/// <summary>Перевірки структури: рефакторинг за SRP, ISP і DIP закріплено тестами.</summary>
public class SolidStructureTests
{
    private static Type[] Dependencies(Type type) =>
        type.GetConstructors().Single().GetParameters().Select(p => p.ParameterType).ToArray();

    [Fact]
    public void Query_service_depends_only_on_the_read_side_of_the_store()
    {
        var dependencies = Dependencies(typeof(RentalQueryService));
        Assert.Equal([typeof(IRentalReader)], dependencies);
        Assert.DoesNotContain(typeof(IRentalReader).GetMethods(), m => m.Name is "Add" or "Save");
    }

    [Fact]
    public void Each_command_handler_exposes_a_single_public_operation()
    {
        foreach (var type in new[] { typeof(OpenRentalHandler), typeof(CompleteRentalHandler), typeof(CancelRentalHandler) })
        {
            var methods = type.GetMethods(System.Reflection.BindingFlags.Public | System.Reflection.BindingFlags.Instance
                | System.Reflection.BindingFlags.DeclaredOnly);
            Assert.Equal("HandleAsync", Assert.Single(methods).Name);
        }
    }

    [Fact]
    public void Only_open_handler_depends_on_pricing()
    {
        Assert.Contains(typeof(IPricingService), Dependencies(typeof(OpenRentalHandler)));
        Assert.DoesNotContain(typeof(IPricingService), Dependencies(typeof(CompleteRentalHandler)));
        Assert.DoesNotContain(typeof(IPricingService), Dependencies(typeof(CancelRentalHandler)));
    }

    [Fact]
    public void Audit_handler_depends_on_abstraction_not_on_file_system()
    {
        Assert.Equal([typeof(IAuditLog)], Dependencies(typeof(AuditLogHandler)));
    }

    private sealed class MemoryAuditLog : IAuditLog
    {
        public List<(string Name, object Payload)> Entries { get; } = [];
        public Task WriteAsync(string eventName, object payload, CancellationToken ct)
        {
            Entries.Add((eventName, payload));
            return Task.CompletedTask;
        }
    }

    [Fact]
    public async Task Audit_handler_is_testable_without_a_file()
    {
        var log = new MemoryAuditLog();
        var handler = new AuditLogHandler(log);
        var evt = new RentalCancelled(Guid.NewGuid(), Guid.NewGuid());
        await handler.HandleAsync(evt, default);
        Assert.Equal([("RentalCancelled", (object)evt)], log.Entries);
    }

    [Fact]
    public async Task File_audit_log_appends_one_line_per_entry()
    {
        var path = Path.Combine(Path.GetTempPath(), $"carrental-audit-{Guid.NewGuid():N}.log");
        try
        {
            var log = new FileAuditLog(path);
            await log.WriteAsync("RentalCancelled", new RentalCancelled(Guid.NewGuid(), Guid.NewGuid()), default);
            await log.WriteAsync("RentalCancelled", new RentalCancelled(Guid.NewGuid(), Guid.NewGuid()), default);
            var lines = await File.ReadAllLinesAsync(path);
            Assert.Equal(2, lines.Length);
            Assert.All(lines, l => Assert.Contains("RentalCancelled", l));
        }
        finally { File.Delete(path); }
    }
}
