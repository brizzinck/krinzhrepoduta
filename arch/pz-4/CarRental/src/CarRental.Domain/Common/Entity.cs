namespace CarRental.Domain.Common;

/// <summary>Базова сутність: ідентичність визначається Id, а не значеннями полів.</summary>
public abstract class Entity
{
    public Guid Id { get; private set; } = Guid.NewGuid();

    /// <summary>Короткий опис для журналів; похідні класи перевизначають.</summary>
    public virtual string Describe() => $"{GetType().Name} {Id}";
}
