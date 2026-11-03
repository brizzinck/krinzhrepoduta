using FluentValidation;

namespace CarRental.WebApi.Validation;

// Перевірки формату на межі API: виконуються автоматично до виклику дії контролера.

public sealed class RegisterCarRequestValidator : AbstractValidator<RegisterCarRequest>
{
    public RegisterCarRequestValidator()
    {
        RuleFor(x => x.Plate)
            .Cascade(CascadeMode.Stop)
            .NotEmpty().WithMessage("Вкажіть номер автомобіля.")
            .Matches("^[A-Za-z]{2}[0-9]{4}[A-Za-z]{2}$").WithMessage("Номер має формат AA1234BB.");
        RuleFor(x => x.Model)
            .Cascade(CascadeMode.Stop)
            .NotEmpty().WithMessage("Вкажіть модель.")
            .MaximumLength(60).WithMessage("Модель має містити до 60 символів.");
        RuleFor(x => x.Class).NotEmpty().WithMessage("Вкажіть клас автомобіля.");
        RuleFor(x => x.DailyRate)
            .GreaterThan(0).WithMessage("Добова ставка має бути більшою за нуль.")
            .LessThanOrEqualTo(100_000).WithMessage("Добова ставка завелика.");
        RuleFor(x => x.Mileage).GreaterThanOrEqualTo(0).WithMessage("Пробіг не може бути від'ємним.");
    }
}

public sealed class RegisterRenterRequestValidator : AbstractValidator<RegisterRenterRequest>
{
    public RegisterRenterRequestValidator()
    {
        RuleFor(x => x.FullName)
            .Cascade(CascadeMode.Stop)
            .NotEmpty().WithMessage("Вкажіть ПІБ.")
            .Length(2, 120).WithMessage("ПІБ має містити від 2 до 120 символів.");
        RuleFor(x => x.Email)
            .Cascade(CascadeMode.Stop)
            .NotEmpty().WithMessage("Вкажіть електронну адресу.")
            .EmailAddress().WithMessage("Електронна адреса має некоректний формат.");
        RuleFor(x => x.BirthDate).NotEmpty().WithMessage("Вкажіть дату народження.");
        RuleFor(x => x.LicenseNumber)
            .Cascade(CascadeMode.Stop)
            .NotEmpty().WithMessage("Вкажіть номер посвідчення.")
            .Length(5, 20).WithMessage("Номер посвідчення має містити від 5 до 20 символів.");
        RuleFor(x => x.LicenseValidUntil).NotEmpty().WithMessage("Вкажіть строк дії посвідчення.");
    }
}

public sealed class OpenRentalRequestValidator : AbstractValidator<OpenRentalRequest>
{
    public OpenRentalRequestValidator()
    {
        RuleFor(x => x.CarId).NotEmpty().WithMessage("Вкажіть автомобіль.");
        RuleFor(x => x.RenterId).NotEmpty().WithMessage("Вкажіть орендаря.");
        RuleFor(x => x.Start).NotEmpty().WithMessage("Вкажіть дату початку.");
        RuleFor(x => x.End)
            .Cascade(CascadeMode.Stop)
            .NotEmpty().WithMessage("Вкажіть дату повернення.")
            .GreaterThan(x => x.Start).WithMessage("Дата повернення має бути пізніше за дату початку.")
            .Must((request, end) => end.DayNumber - request.Start.DayNumber <= 30)
            .WithMessage("Оренда не може тривати довше 30 днів.");
    }
}

public sealed class CompleteRentalRequestValidator : AbstractValidator<CompleteRentalRequest>
{
    public CompleteRentalRequestValidator()
    {
        RuleFor(x => x.EndMileage).GreaterThanOrEqualTo(0).WithMessage("Пробіг не може бути від'ємним.");
    }
}
