package ua.duikt.lavrishko.rental.auth;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.SecureRandom;
import java.time.Clock;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Годинник і ключ підпису. Годинник винесено в біну, щоб тести могли «перемотувати» час. */
@Configuration
public class ClockConfig {
    private static final Logger log = LoggerFactory.getLogger(ClockConfig.class);

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /** Ключ HS256: зі змінної JWT_SECRET (base64, не менше 32 байт) або випадковий на час роботи. */
    @Bean
    public SecretKey jwtKey(@Value("${app.jwt.secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            log.warn("JWT_SECRET не задано: згенеровано випадковий ключ, токени не переживуть перезапуск");
            return Keys.hmacShaKeyFor(random);
        }
        byte[] bytes = Decoders.BASE64.decode(secret.trim());
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET має містити щонайменше 32 байти (після base64)");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
