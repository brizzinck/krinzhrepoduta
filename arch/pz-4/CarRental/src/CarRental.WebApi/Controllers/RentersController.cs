using CarRental.Application.Rentals;
using Microsoft.AspNetCore.Mvc;

namespace CarRental.WebApi.Controllers;

[ApiController]
[Route("api/renters")]
public sealed class RentersController(RenterService renters) : ControllerBase
{
    [HttpGet("{id:guid}")]
    public Task<RenterDto> Get(Guid id, CancellationToken ct) => renters.Get(id, ct);

    [HttpPost]
    public async Task<IActionResult> Register(RegisterRenterRequest request, CancellationToken ct)
    {
        var renter = await renters.Register(request.FullName, request.Email, request.BirthDate,
            request.LicenseNumber, request.LicenseValidUntil, ct);
        return Created($"/api/renters/{renter.Id}", renter);
    }
}
