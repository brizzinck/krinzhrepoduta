package ua.duikt.lavrishko.rental.web;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.duikt.lavrishko.rental.dto.RegistrationForm;
import ua.duikt.lavrishko.rental.repo.UserRepository;

/** JSON-варіант тієї самої реєстрації (для клієнтів без браузера); правила валідації спільні з формою. */
@RestController
@RequestMapping("/api")
public class RegistrationApiController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public RegistrationApiController(UserRepository users) {
        this.users = users;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegistrationForm form) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (users.existsByLogin(form.getLogin()) || users.existsByEmail(form.getEmail())) {
            body.put("error", "Логін або пошта вже зайняті");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        try {
            users.insert(form, encoder.encode(form.getPassword()));
        } catch (DuplicateKeyException race) {
            body.put("error", "Логін або пошта вже зайняті");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        body.put("status", "created");
        body.put("login", form.getLogin());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
