using CarRental.Domain.Common;

namespace CarRental.Application.Rentals.Pricing;

/// <summary>
/// Надбавка 10%, якщо в оренду потрапляє субота чи неділя. Приклад розширення без зміни наявного коду:
/// це окремий файл і два рядки реєстрації: умова `if` та `AddScoped`. Вмикається налаштуванням Pricing:WeekendSurcharge.
/// </summary>
public sealed class WeekendSurchargeRule : IPricingRule
{
    public int Order => 30;

    public Money Apply(Money current, PricingContext context)
    {
        for (var day = context.Period.Start; day < context.Period.End; day = day.AddDays(1))
            if (day.DayOfWeek is DayOfWeek.Saturday or DayOfWeek.Sunday)
                return current.Multiply(1.10m);
        return current;
    }
}
