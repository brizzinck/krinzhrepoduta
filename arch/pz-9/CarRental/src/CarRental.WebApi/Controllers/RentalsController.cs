using AutoMapper;
using CarRental.Application.Rentals.Dto;
using CarRental.Application.Rentals.UseCases;
using Microsoft.AspNetCore.Mvc;

namespace CarRental.WebApi.Controllers;

[ApiController]
[Route("api/rentals")]
public sealed class RentalsController(
    OpenRentalHandler open,
    CompleteRentalHandler complete,
    CancelRentalHandler cancel,
    IRentalQueryService queries,
    IMapper mapper) : ControllerBase
{
    [HttpGet]
    public Task<IReadOnlyList<RentalListItemDto>> List([FromQuery] Guid? renterId, CancellationToken ct) =>
        queries.List(renterId, ct);

    [HttpGet("{id:guid}")]
    public Task<RentalDetailsDto> Get(Guid id, CancellationToken ct) => queries.Get(id, ct);

    [HttpPost]
    public async Task<IActionResult> Open(OpenRentalRequest request, CancellationToken ct)
    {
        var rental = await open.HandleAsync(mapper.Map<OpenRentalCommand>(request), ct);
        return Created($"/api/rentals/{rental.Id}", rental);
    }

    [HttpPost("{id:guid}/complete")]
    public Task<RentalClosedDto> Complete(Guid id, CompleteRentalRequest request, CancellationToken ct) =>
        complete.HandleAsync(new CompleteRentalCommand(id, request.EndMileage), ct);

    [HttpPost("{id:guid}/cancel")]
    public Task<RentalClosedDto> Cancel(Guid id, CancellationToken ct) =>
        cancel.HandleAsync(new CancelRentalCommand(id), ct);
}
