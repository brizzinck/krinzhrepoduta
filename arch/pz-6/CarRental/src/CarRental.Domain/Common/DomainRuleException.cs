namespace CarRental.Domain.Common;

/// <summary>Порушення бізнес-правила. WebApi перетворює його на HTTP 400.</summary>
public class DomainRuleException(string message) : Exception(message);

/// <summary>Порушення, що виникає через конфлікт зі станом системи (перетин оренд, дубль). HTTP 409.</summary>
public sealed class DomainConflictException(string message) : DomainRuleException(message);
