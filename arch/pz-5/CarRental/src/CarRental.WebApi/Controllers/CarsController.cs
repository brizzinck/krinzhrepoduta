using CarRental.Application.Fleet;
using Microsoft.AspNetCore.Mvc;

namespace CarRental.WebApi.Controllers;

/// <summary>Тонкий контролер: приймає HTTP, делегує сценарій Application, повертає результат.</summary>
[ApiController]
[Route("api/cars")]
public sealed class CarsController(CarService cars) : ControllerBase
{
    [HttpGet]
    public Task<IReadOnlyList<CarDto>> List(CancellationToken ct) => cars.List(ct);

    [HttpGet("{id:guid}")]
    public Task<CarDto> Get(Guid id, CancellationToken ct) => cars.Get(id, ct);

    [HttpPost]
    public async Task<IActionResult> Register(RegisterCarRequest request, CancellationToken ct)
    {
        var car = await cars.Register(request.Plate, request.Model, request.Class, request.DailyRate, request.Mileage, ct);
        return Created($"/api/cars/{car.Id}", car);
    }

    [HttpPost("{id:guid}/maintenance")]
    public Task<CarDto> SendToMaintenance(Guid id, CancellationToken ct) => cars.SendToMaintenance(id, ct);
}
