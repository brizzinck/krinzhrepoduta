namespace CarRental.Application.Common;

/// <summary>Запитаний об'єкт не знайдено. WebApi перетворює на HTTP 404.</summary>
public sealed class NotFoundException(string what) : Exception($"{what} не знайдено.");

/// <summary>Дані змінилися після читання; операцію треба повторити з актуальними даними. HTTP 409.</summary>
public sealed class ConcurrentChangeException() : Exception("Дані одночасно змінені іншим запитом. Оновіть їх і повторіть операцію.");
