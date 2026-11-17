package ua.duikt.lavrishko.rental.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Журнал невдалих спроб: один рядок на подію, без токена й пароля. */
@Component
public class AuditLog {
    private static final Logger LOG = LoggerFactory.getLogger("security-audit");

    public void loginFailed(String login, String ip) {
        LOG.info("LOGIN_FAILED login={} ip={}", clean(login), ip);
    }

    public void tokenRejected(TokenRejected e, String ip, String path, String token) {
        LOG.info("TOKEN_REJECTED reason={} ip={} path={} sub={} jti={} fp={}",
                e.reason(), ip, clean(path), clean(e.sub()), clean(e.jti()), fingerprint(token));
    }

    /** Прибирає керувальні символи й усе зайве, щоб введення не підробило рядок журналу. */
    static String clean(String value) {
        if (value == null) {
            return "-";
        }
        String cut = value.length() > 64 ? value.substring(0, 64) : value;
        return cut.replaceAll("[^\\p{L}\\p{N}_.@/-]", "?");
    }

    /** Перші 8 hex-символів SHA-256 токена: за ними можна зіставити події, але токен не відновити. */
    static String fingerprint(String token) {
        if (token == null || token.isEmpty()) {
            return "-";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (Exception e) {
            return "-";
        }
    }
}
