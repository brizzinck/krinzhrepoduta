package ua.duikt.lavrishko.rental.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.audit.file=target/test-auth-failures.log")
@AutoConfigureMockMvc
class TokenAuthTest {
    private static final byte[] TEST_KEY = new byte[64];
    private static final Instant START = Instant.parse("2026-11-17T10:00:00Z");

    static {
        // Ключ тестів випадковий для кожного запуску й ніде не зберігається
        new SecureRandom().nextBytes(TEST_KEY);
    }

    @DynamicPropertySource
    static void secret(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> Base64.getEncoder().encodeToString(TEST_KEY));
    }

    @TestConfiguration
    static class ClockOverride {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(START);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired MutableClock clock;
    @Autowired SecretKey key;
    @Autowired ObjectMapper json;

    private final ListAppender<ILoggingEvent> audit = new ListAppender<>();
    private Logger auditLogger;

    @BeforeEach
    void attachAppender() {
        clock.reset();
        auditLogger = (Logger) LoggerFactory.getLogger("security-audit");
        audit.list.clear();
        audit.start();
        auditLogger.addAppender(audit);
    }

    @AfterEach
    void detachAppender() {
        auditLogger.detachAppender(audit);
        audit.stop();
    }

    private String loginBody(String login, String password) {
        return "{\"login\":\"" + login + "\",\"password\":\"" + password + "\"}";
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("olena_k", "Secret2026")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private void expectRejected(String authorization, String reason) throws Exception {
        var request = get("/api/profile");
        if (authorization != null) {
            request = request.header("Authorization", authorization);
        }
        mvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer error=\"invalid_token\""))
                .andExpect(jsonPath("$.error").value("invalid_token"))
                .andExpect(jsonPath("$.reason").value(reason));
    }

    @Test
    void loginIssuesTokenValidForTwoMinutes() throws Exception {
        String token = login();
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        JsonNode payload = json.readTree(Base64.getUrlDecoder().decode(parts[1]));
        assertThat(payload.get("exp").asLong() - payload.get("iat").asLong()).isEqualTo(120);
        assertThat(payload.get("iss").asText()).isEqualTo("car-rental-lr6");
        assertThat(payload.get("sub").asText()).isEqualTo("olena_k");
        assertThat(payload.get("role").asText()).isEqualTo("CLIENT");
        assertThat(payload.get("jti").asText()).isNotBlank();
    }

    @Test
    void wrongPasswordGivesUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("olena_k", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"invalid_credentials\"}", true));
    }

    @Test
    void unknownUserGetsTheSameAnswer() throws Exception {
        String wrongPassword = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("olena_k", "wrong-password")))
                .andReturn().getResponse().getContentAsString();
        String unknownUser = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("nobody", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertThat(unknownUser).isEqualTo(wrongPassword);
    }

    @Test
    void emptyLoginFailsValidation() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("", "x")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordLongerThan72BytesIsRejected() throws Exception {
        // 40 кириличних літер це 80 байтів: за довжиною в символах пароль проходив би, а BCrypt відкинув би хвіст
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("olena_k", "ї".repeat(40))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profileWithValidTokenIsOk() throws Exception {
        String token = login();
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("olena_k"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.secondsLeft").value(120));
    }

    @Test
    void tokenStillWorksAt119Seconds() throws Exception {
        String token = login();
        clock.advance(Duration.ofSeconds(119));
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secondsLeft").value(1));
    }

    @Test
    void tokenExpiresAfter121Seconds() throws Exception {
        String token = login();
        clock.advance(Duration.ofSeconds(121));
        expectRejected("Bearer " + token, "expired");
    }

    @Test
    void missingHeaderIsRejected() throws Exception {
        expectRejected(null, "missing");
    }

    @Test
    void basicSchemeIsRejected() throws Exception {
        expectRejected("Basic b2xlbmFfazpTZWNyZXQyMDI2", "missing");
    }

    @Test
    void tamperedPayloadIsRejected() throws Exception {
        String[] parts = login().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("olena_k", "manager").replace("CLIENT", "MANAGER");
        expectRejected("Bearer " + parts[0] + "." + b64(payload) + "." + parts[2], "bad_signature");
    }

    @Test
    void tokenSignedWithOtherKeyIsRejected() throws Exception {
        SecretKey other = Keys.hmacShaKeyFor(new SecureRandom().generateSeed(32));
        String forged = Jwts.builder().issuer(TokenService.ISSUER).subject("manager")
                .issuedAt(Date.from(START)).expiration(Date.from(START.plusSeconds(120)))
                .signWith(other, Jwts.SIG.HS256).compact();
        expectRejected("Bearer " + forged, "bad_signature");
    }

    @Test
    void algNoneIsRejected() throws Exception {
        String[] parts = login().split("\\.");
        String forged = b64("{\"alg\":\"none\",\"typ\":\"JWT\"}") + "." + parts[1] + ".";
        expectRejected("Bearer " + forged, "wrong_alg");
    }

    @Test
    void hs384WithTheSameKeyIsRejected() throws Exception {
        // 64-байтовий ключ дозволяє HS384, тому саму лише перевірку підпису токен би пройшов
        String hs384 = Jwts.builder().issuer(TokenService.ISSUER).subject("olena_k")
                .issuedAt(Date.from(START)).expiration(Date.from(START.plusSeconds(120)))
                .signWith(key, Jwts.SIG.HS384).compact();
        expectRejected("Bearer " + hs384, "wrong_alg");
    }

    @Test
    void wrongIssuerIsRejected() throws Exception {
        String foreign = Jwts.builder().issuer("other-service").subject("olena_k")
                .issuedAt(Date.from(START)).expiration(Date.from(START.plusSeconds(120)))
                .signWith(key, Jwts.SIG.HS256).compact();
        expectRejected("Bearer " + foreign, "wrong_issuer");
    }

    @Test
    void garbageIsRejected() throws Exception {
        expectRejected("Bearer not-a-token", "malformed");
    }

    @Test
    void pathParameterAndEncodedPathDoNotBypassFilter() throws Exception {
        // Шлях нормалізується до /api/profile, тому фільтр має спрацювати й тут
        for (String path : new String[] {"/api;x/profile", "/%61pi/profile"}) {
            var response = mvc.perform(get(URI.create(path))).andReturn().getResponse();
            assertThat(response.getStatus()).as(path).isEqualTo(401);
            assertThat(response.getContentAsString()).as(path).contains("\"reason\":\"missing\"");
        }
        assertThat(audit.list).hasSize(2);
    }

    @Test
    void auditLogHasOneLinePerRejectionAndNoFullToken() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("olena_k\\nFAKE", "wrong-password")));
        String token = login();
        clock.advance(Duration.ofSeconds(121));
        expectRejected("Bearer " + token, "expired");
        expectRejected("Bearer not-a-token", "malformed");
        expectRejected(null, "missing");

        assertThat(audit.list).hasSize(4);
        var lines = audit.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(lines.get(0)).startsWith("LOGIN_FAILED login=").doesNotContain("\n");
        assertThat(lines.get(1)).startsWith("TOKEN_REJECTED reason=expired").contains("sub=olena_k", "jti=", "fp=");
        assertThat(lines.get(2)).startsWith("TOKEN_REJECTED reason=malformed");
        assertThat(lines.get(3)).startsWith("TOKEN_REJECTED reason=missing");
        assertThat(lines).noneMatch(l -> l.contains(token) || l.contains("wrong-password"));
    }
}
