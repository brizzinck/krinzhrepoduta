package ua.duikt.lavrishko.rental.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Вхід: за правильного логіна й пароля видає токен на 2 хвилини. */
@RestController
public class AuthController {
    public record LoginRequest(String login, String password) {
    }

    private final UserStore users;
    private final TokenService tokens;
    private final AuditLog audit;

    public AuthController(UserStore users, TokenService tokens, AuditLog audit) {
        this.users = users;
        this.tokens = tokens;
        this.audit = audit;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest body, HttpServletRequest request) {
        // Серверна перевірка вхідних даних: порожні й задовгі значення не доходять до BCrypt
        if (body == null || isBad(body.login(), 64) || isBad(body.password(), 72)) {
            return ResponseEntity.badRequest().body(Map.of("error", "bad_request"));
        }
        Optional<User> user = users.authenticate(body.login(), body.password());
        if (user.isEmpty()) {
            audit.loginFailed(body.login(), request.getRemoteAddr());
            // Однакова відповідь і для неправильного пароля, і для невідомого логіна
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }
        String token = tokens.issue(user.get());
        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "expiresIn", TokenService.TTL_SECONDS));
    }

    private static boolean isBad(String value, int max) {
        return value == null || value.isBlank() || value.length() > max;
    }
}
