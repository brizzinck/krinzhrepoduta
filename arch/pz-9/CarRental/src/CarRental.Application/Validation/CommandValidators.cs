using CarRental.Application.Common;
using CarRental.Application.Common.Persistence;
using CarRental.Application.Fleet.Dto;
using CarRental.Application.Rentals;
using CarRental.Application.Rentals.Dto;
using CarRental.Application.Rentals.UseCases;
using CarRental.Domain.Common;
using CarRental.Domain.Fleet;
using CarRental.Domain.Rentals;
using FluentValidation;

namespace CarRental.Application.Validation;

// Семантичні перевірки команд: потребують годинника або БД, тому асинхронні й виконуються на початку сценарію.
// Відсутній автомобіль чи орендар тут не помилка валідації: сценарій поверне 404.

public sealed class OpenRentalCommandValidator : AbstractValidator<OpenRentalCommand>
{
    public OpenRentalCommandValidator(IUnitOfWork uow, IFleetGateway fleet, IClock clock)
    {
        RuleFor(x => x.CarId)
            .NotEmpty().WithMessage("Вкажіть автомобіль.")
            .MustAsync(async (id, ct) => await fleet.GetRentableCar(id, ct) is not { IsRentable: false })
            .WithMessage("Автомобіль зараз недоступний для оренди.");
        RuleFor(x => x.Start)
            .Must(start => start >= clock.Today).WithMessage("Оренда не може починатися в минулому.");
        RuleFor(x => x.RenterId)
            .NotEmpty().WithMessage("Вкажіть орендаря.")
            .CustomAsync(async (id, context, ct) =>
            {
                var renter = await uow.Renters.GetByIdAsync(id, ct);
                if (renter is null)
                    return;
                var cmd = context.InstanceToValidate;
                if (renter.AgeOn(cmd.Start) < Renter.MinAge)
                    context.AddFailure($"Мінімальний вік орендаря {Renter.MinAge} рік на дату початку оренди.");
                if (!renter.License.IsValidThrough(cmd.End))
                    context.AddFailure("Посвідчення водія не діє до кінця оренди.");
            });
    }
}

public sealed class RegisterCarCommandValidator : AbstractValidator<RegisterCarCommand>
{
    public RegisterCarCommandValidator(IUnitOfWork uow)
    {
        RuleFor(x => x.Class)
            .IsEnumName(typeof(CarClass), caseSensitive: false)
            .WithMessage("Невідомий клас автомобіля. Допустимі: Economy, Comfort, Premium.");
        RuleFor(x => x.Plate)
            .MustAsync(async (plate, ct) => TryPlate(plate) is not { } value || !await uow.Cars.PlateExistsAsync(value, ct))
            .WithMessage("Автомобіль із таким номером уже зареєстрований.");
    }

    // Неправильний формат тут не перевіряємо: його відхиляє API, а за прямого виклику конструктор LicensePlate.
    private static LicensePlate? TryPlate(string? plate)
    {
        try { return new LicensePlate(plate); }
        catch (DomainRuleException) { return null; }
    }
}

public sealed class RegisterRenterCommandValidator : AbstractValidator<RegisterRenterCommand>
{
    public RegisterRenterCommandValidator(IClock clock)
    {
        RuleFor(x => x.BirthDate)
            .Must(date => date < clock.Today).WithMessage("Дата народження має бути в минулому.");
    }
}

public sealed class CompleteRentalCommandValidator : AbstractValidator<CompleteRentalCommand>
{
    public CompleteRentalCommandValidator()
    {
        RuleFor(x => x.RentalId).NotEmpty().WithMessage("Вкажіть оренду.");
        RuleFor(x => x.EndMileage).GreaterThanOrEqualTo(0).WithMessage("Пробіг не може бути від'ємним.");
    }
}

public sealed class CancelRentalCommandValidator : AbstractValidator<CancelRentalCommand>
{
    public CancelRentalCommandValidator()
    {
        RuleFor(x => x.RentalId).NotEmpty().WithMessage("Вкажіть оренду.");
    }
}
