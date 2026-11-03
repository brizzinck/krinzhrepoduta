using System.Reflection;
using CarRental.Application.Common.Persistence;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;
using CarRental.Infrastructure.Persistence;
using CarRental.WebApi.Controllers;

namespace CarRental.Tests.Architecture;

/// <summary>Правила стилю, які перевіряє збірка: напрям залежностей шарів і межа між контекстами.</summary>
public class ArchitectureRulesTests
{
    private static string[] References(Assembly assembly) =>
        assembly.GetReferencedAssemblies().Select(a => a.Name!).Where(n => n.StartsWith("CarRental")).OrderBy(n => n).ToArray();

    [Fact]
    public void Domain_references_no_other_layer() =>
        Assert.Empty(References(typeof(Entity).Assembly));

    [Fact]
    public void Application_references_only_domain() =>
        Assert.Equal(["CarRental.Domain"], References(typeof(IUnitOfWork).Assembly));

    [Fact]
    public void Infrastructure_does_not_reference_web_api() =>
        Assert.DoesNotContain("CarRental.WebApi", References(typeof(CarRentalDbContext).Assembly));

    [Fact]
    public void Controllers_do_not_reference_infrastructure_types()
    {
        var infrastructure = typeof(CarRentalDbContext).Assembly;
        var dependencies = typeof(CarsController).Assembly.GetTypes()
            .Where(t => t.Name.EndsWith("Controller"))
            .SelectMany(t => t.GetConstructors().SelectMany(c => c.GetParameters()));
        Assert.DoesNotContain(dependencies, p => p.ParameterType.Assembly == infrastructure);
    }

    [Fact]
    public void Rentals_context_of_the_domain_does_not_use_fleet_types()
    {
        var fleet = typeof(Car).Namespace!;
        var rentals = typeof(Rental).Assembly.GetTypes().Where(t => t.Namespace == typeof(Rental).Namespace);
        const BindingFlags all = BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Static | BindingFlags.DeclaredOnly;
        foreach (var type in rentals)
        {
            var used = type.GetProperties(all).Select(p => p.PropertyType)
                .Concat(type.GetFields(all).Select(f => f.FieldType))
                .Concat(type.GetMethods(all).SelectMany(m => m.GetParameters().Select(p => p.ParameterType).Append(m.ReturnType)));
            Assert.DoesNotContain(used, t => t.Namespace == fleet);
        }
    }
}
