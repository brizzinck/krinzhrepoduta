package ua.duikt.lavrishko.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import ua.duikt.lavrishko.rental.validation.BirthDate;
import ua.duikt.lavrishko.rental.validation.MaxBytes;
import ua.duikt.lavrishko.rental.validation.Patterns;

/**
 * Дані форми реєстрації. Усі правила діють на сервері незалежно від клієнтського JavaScript.
 * Поля «role» тут немає навмисно: зайвий параметр запиту не потрапить у модель (захист від mass assignment).
 * Порожній або пробільний рядок сеттер зберігає як null: тоді @NotBlank дає одне повідомлення «Вкажіть …»,
 * а @Size/@Pattern порожнє значення пропускають, тож помилки формату решти полів показуються в тій самій відповіді.
 * Сеттери працюють і для форми, і для JSON (/api/register).
 */
public class RegistrationForm {

    @NotBlank(message = "Вкажіть логін")
    @Size(min = 5, max = 32, message = "Логін має містити від 5 до 32 символів")
    @Pattern(regexp = Patterns.LOGIN, message = "Логін може містити лише латинські літери, цифри та символи _ . -")
    private String login;

    @NotBlank(message = "Вкажіть ім'я")
    @Size(max = 50, message = "Ім'я не може бути довшим за 50 символів")
    @Pattern(regexp = Patterns.NAME, message = "Ім'я має містити лише літери")
    private String firstName;

    @NotBlank(message = "Вкажіть e-mail")
    @Size(max = 254, message = "E-mail не може бути довшим за 254 символи")
    @Pattern(regexp = Patterns.EMAIL, message = "E-mail має містити @ та доменне ім'я, наприклад name@example.com")
    private String email;

    @NotBlank(message = "Вкажіть пароль")
    @Size(min = 8, max = 72, message = "Пароль має містити від 8 до 72 символів")
    @MaxBytes(value = 72, message = "Пароль задовгий: допустимо не більше 72 байтів у UTF-8")
    @Pattern(regexp = Patterns.HAS_UPPER, message = "Пароль має містити принаймні одну велику літеру")
    @Pattern(regexp = Patterns.HAS_DIGIT, message = "Пароль має містити принаймні одну цифру")
    private String password;

    @NotBlank(message = "Вкажіть номер телефону")
    @Pattern(regexp = Patterns.PHONE, message = "Телефон: необов'язковий + і від 10 до 15 цифр, без пробілів")
    private String phone;

    @NotNull(message = "Вкажіть дату народження у форматі РРРР-ММ-ДД")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @BirthDate(minYear = 1900, minAge = 18)
    private LocalDate birthDate;

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = blankToNull(login); }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = blankToNull(firstName); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = blankToNull(email); }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = blankToNull(password); }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = blankToNull(phone); }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    @Override
    public String toString() {
        // пароль у рядкове подання не потрапляє, щоб не опинитися в журналах
        return "RegistrationForm{login=" + login + ", email=" + email + "}";
    }
}
