package ua.duikt.lavrishko.rental.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ЛР 6. Аутентифікація за токеном (JWT) для навчального сервісу «Прокат автомобілів». */
@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
