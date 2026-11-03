using AutoMapper;
using CarRental.Application.Fleet;
using CarRental.Application.Fleet.Dto;
using Microsoft.AspNetCore.Mvc;

namespace CarRental.WebApi.Controllers;

/// <summary>Тонкий контролер: приймає HTTP, мапить моделі, делегує сценарій Application.</summary>
[ApiController]
[Route("api/cars")]
public sealed class CarsController(CarService cars, IMapper mapper) : ControllerBase
{
    [HttpGet]
    public async Task<IReadOnlyList<CarResponse>> List(CancellationToken ct) =>
        mapper.Map<IReadOnlyList<CarResponse>>(await cars.List(ct));

    [HttpGet("{id:guid}")]
    public async Task<CarResponse> Get(Guid id, CancellationToken ct) =>
        mapper.Map<CarResponse>(await cars.Get(id, ct));

    [HttpPost]
    public async Task<IActionResult> Register(RegisterCarRequest request, CancellationToken ct)
    {
        var car = await cars.Register(mapper.Map<RegisterCarCommand>(request), ct);
        return Created($"/api/cars/{car.Id}", mapper.Map<CarResponse>(car));
    }

    [HttpPost("{id:guid}/maintenance")]
    public async Task<CarResponse> SendToMaintenance(Guid id, CancellationToken ct) =>
        mapper.Map<CarResponse>(await cars.SendToMaintenance(id, ct));
}
