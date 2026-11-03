using CarRental.Application.Common;
using CarRental.Domain.Common;
using FluentValidation;
using Microsoft.AspNetCore.Mvc.Infrastructure;
using Microsoft.AspNetCore.Mvc.ModelBinding;

namespace CarRental.WebApi;

/// <summary>Перетворює помилки Domain і Application на HTTP-відповіді з тілом {"error": "..."}.</summary>
public sealed class ErrorHandlingMiddleware(RequestDelegate next)
{
    public async Task InvokeAsync(HttpContext context)
    {
        try { await next(context); }
        catch (ValidationException ex)
        {
            // Той самий формат, що й у автоматичної перевірки запитів: усі помилки за полями.
            var modelState = new ModelStateDictionary();
            foreach (var error in ex.Errors)
                modelState.AddModelError(error.PropertyName, error.ErrorMessage);
            var factory = context.RequestServices.GetRequiredService<ProblemDetailsFactory>();
            var problem = factory.CreateValidationProblemDetails(context, modelState, StatusCodes.Status400BadRequest);
            context.Response.StatusCode = StatusCodes.Status400BadRequest;
            await context.Response.WriteAsJsonAsync(problem, options: null, contentType: "application/problem+json");
        }
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
