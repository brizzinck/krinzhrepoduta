package ua.duikt.lavrishko.rental.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Захищений ендпоінт: доходить сюди лише запит із чинним токеном (його перевірив JwtAuthFilter). */
@RestController
public class ProfileController {
    private final TokenService tokens;

    public ProfileController(TokenService tokens) {
        this.tokens = tokens;
    }

    @GetMapping("/api/profile")
    public ResponseEntity<Map<String, Object>> profile(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute(JwtAuthFilter.CLAIMS);
        if (claims == null) {
            // Друга лінія захисту: без перевіреного токена дані не віддаються, навіть якщо фільтр обійдено
            return ResponseEntity.status(401).build();
        }
        Instant exp = claims.getExpiration().toInstant();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("login", claims.getSubject());
        out.put("role", claims.get("role", String.class));
        out.put("issuedAt", claims.getIssuedAt().toInstant().toString());
        out.put("expiresAt", exp.toString());
        out.put("secondsLeft", Math.max(0, exp.getEpochSecond() - tokens.now().getEpochSecond()));
        return ResponseEntity.ok(out);
    }
}
