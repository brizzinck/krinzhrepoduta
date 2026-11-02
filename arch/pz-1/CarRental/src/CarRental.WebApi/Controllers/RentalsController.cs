using CarRental.Application.Rentals;
using Microsoft.AspNetCore.Mvc;

namespace CarRental.WebApi.Controllers;

[ApiController]
[Route("api/rentals")]
public sealed class RentalsController(RentalService rentals) : ControllerBase
{
    [HttpGet]
    public Task<IReadOnlyList<RentalDto>> List([FromQuery] Guid? renterId, CancellationToken ct) =>
        rentals.List(renterId, ct);

    [HttpGet("{id:guid}")]
    public Task<RentalDto> Get(Guid id, CancellationToken ct) => rentals.Get(id, ct);

    [HttpPost]
    public async Task<IActionResult> Open(OpenRentalCommand command, CancellationToken ct)
    {
        var rental = await rentals.Open(command, ct);
        return Created($"/api/rentals/{rental.Id}", rental);
    }

    [HttpPost("{id:guid}/complete")]
    public Task<RentalDto> Complete(Guid id, CompleteRentalRequest request, CancellationToken ct) =>
        rentals.Complete(id, request.EndMileage, ct);

    [HttpPost("{id:guid}/cancel")]
    public Task<RentalDto> Cancel(Guid id, CancellationToken ct) => rentals.Cancel(id, ct);
}
