using CarRental.Application.Rentals;
using CarRental.Application.Rentals.UseCases;
using Microsoft.AspNetCore.Mvc;

namespace CarRental.WebApi.Controllers;

[ApiController]
[Route("api/rentals")]
public sealed class RentalsController(
    OpenRentalHandler open,
    CompleteRentalHandler complete,
    CancelRentalHandler cancel,
    RentalQueryService queries) : ControllerBase
{
    [HttpGet]
    public Task<IReadOnlyList<RentalDto>> List([FromQuery] Guid? renterId, CancellationToken ct) =>
        queries.List(renterId, ct);

    [HttpGet("{id:guid}")]
    public Task<RentalDto> Get(Guid id, CancellationToken ct) => queries.Get(id, ct);

    [HttpPost]
    public async Task<IActionResult> Open(OpenRentalCommand command, CancellationToken ct)
    {
        var rental = await open.HandleAsync(command, ct);
        return Created($"/api/rentals/{rental.Id}", rental);
    }

    [HttpPost("{id:guid}/complete")]
    public Task<RentalDto> Complete(Guid id, CompleteRentalRequest request, CancellationToken ct) =>
        complete.HandleAsync(new CompleteRentalCommand(id, request.EndMileage), ct);

    [HttpPost("{id:guid}/cancel")]
    public Task<RentalDto> Cancel(Guid id, CancellationToken ct) =>
        cancel.HandleAsync(new CancelRentalCommand(id), ct);
}
