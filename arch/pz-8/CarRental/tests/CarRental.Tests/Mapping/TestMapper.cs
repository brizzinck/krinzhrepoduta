using AutoMapper;
using CarRental.Application.Mapping;
using CarRental.WebApi.Mapping;

namespace CarRental.Tests.Mapping;

/// <summary>Та сама конфігурація профілів, що й у застосунку.</summary>
internal static class TestMapper
{
    public static readonly MapperConfiguration Configuration = new(cfg =>
    {
        cfg.AddMaps(typeof(CarProfile).Assembly);
        cfg.AddMaps(typeof(ApiProfile).Assembly);
    });

    public static readonly IMapper Instance = Configuration.CreateMapper();
}
