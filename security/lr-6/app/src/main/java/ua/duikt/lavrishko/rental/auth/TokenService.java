package ua.duikt.lavrishko.rental.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Видача й перевірка JWT (бібліотека jjwt). Підпис HS256, строк дії 2 хвилини. */
@Service
public class TokenService {
    public static final String ISSUER = "car-rental-lr6";
    public static final long TTL_SECONDS = 120;

    private final SecretKey key;
    private final Clock clock;
    private final ObjectMapper json = new ObjectMapper();

    public TokenService(SecretKey key, Clock clock) {
        this.key = key;
        this.clock = clock;
    }

    /** Видає токен: iss, sub, role, iat, exp = iat + 120 с і унікальний jti. */
    public String issue(User user) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.login())
                .claim("role", user.role())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(TTL_SECONDS)))
                .id(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Перевіряє підпис, алгоритм, видавця й строк дії. Нульовий допуск розбіжності годинників. */
    public Claims verify(String token) throws TokenRejected {
        if (token == null || token.isBlank()) {
            throw new TokenRejected("missing");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new TokenRejected("malformed");
        }
        JsonNode header = decode(parts[0]);
        JsonNode payload = decode(parts[1]);
        if (header == null || payload == null) {
            throw new TokenRejected("malformed");
        }
        String sub = text(payload, "sub");
        String jti = text(payload, "jti");
        // Алгоритм перевіряємо явно: jjwt прийняв би й HS384/HS512, якщо ключ достатньо довгий
        if (!"HS256".equals(text(header, "alg"))) {
            throw new TokenRejected("wrong_alg", sub, jti);
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getExpiration() == null) {
                throw new TokenRejected("malformed", sub, jti);   // токен без строку дії не приймаємо
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new TokenRejected("expired", sub, jti);
        } catch (SignatureException e) {
            throw new TokenRejected("bad_signature", sub, jti);
        } catch (IncorrectClaimException | MissingClaimException e) {
            throw new TokenRejected("wrong_issuer", sub, jti);
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenRejected("malformed", sub, jti);
        }
    }

    public Instant now() {
        return clock.instant();
    }

    private JsonNode decode(String part) {
        try {
            JsonNode node = json.readTree(new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8));
            return node != null && node.isObject() ? node : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && v.isTextual() ? v.asText() : null;
    }
}
