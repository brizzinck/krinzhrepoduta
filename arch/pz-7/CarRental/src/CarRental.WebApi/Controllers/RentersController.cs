using AutoMapper;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Dto;
using Microsoft.AspNetCore.Mvc;

namespace CarRental.WebApi.Controllers;

[ApiController]
[Route("api/renters")]
public sealed class RentersController(RenterService renters, IMapper mapper) : ControllerBase
{
    [HttpGet("{id:guid}")]
    public Task<RenterDto> Get(Guid id, CancellationToken ct) => renters.Get(id, ct);

    [HttpPost]
    public async Task<IActionResult> Register(RegisterRenterRequest request, CancellationToken ct)
    {
        var renter = await renters.Register(mapper.Map<RegisterRenterCommand>(request), ct);
        return Created($"/api/renters/{renter.Id}", renter);
    }
}
