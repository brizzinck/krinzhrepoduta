using CarRental.Application.Common;
using CarRental.Domain.Common;

namespace CarRental.WebApi;

/// <summary>Перетворює помилки Domain і Application на HTTP-відповіді з тілом {"error": "..."}.</summary>
public sealed class ErrorHandlingMiddleware(RequestDelegate next)
{
    public async Task InvokeAsync(HttpContext context)
    {
        try { await next(context); }
        catch (Exception ex) when (ex is DomainRuleException or NotFoundException or ConcurrentChangeException)
        {
            context.Response.StatusCode = ex switch
            {
                NotFoundException => StatusCodes.Status404NotFound,
                DomainConflictException or ConcurrentChangeException => StatusCodes.Status409Conflict,
                _ => StatusCodes.Status400BadRequest
            };
            await context.Response.WriteAsJsonAsync(new { error = ex.Message });
        }
    }
}
