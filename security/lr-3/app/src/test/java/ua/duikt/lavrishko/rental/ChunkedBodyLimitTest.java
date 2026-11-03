package ua.duikt.lavrishko.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Ліміт тіла для chunked-запитів (без Content-Length) на справжньому Tomcat:
 * MockMvc завжди знає довжину тіла, тому тут запит іде через мережу.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChunkedBodyLimitTest {
    @LocalServerPort
    int port;

    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    /** Тіло з невідомою наперед довжиною: HttpClient надсилає його частинами (Transfer-Encoding: chunked). */
    private HttpResponse<String> postChunked(String path, String contentType, String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(bytes)))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    @Test
    void chunkedJsonOverLimitGives413() throws Exception {
        String json = "{\"login\":\"" + "a".repeat(40_000) + "\"}";
        HttpResponse<String> response = postChunked("/api/register", "application/json", json);
        assertEquals(413, response.statusCode(), response.body());
        assertTrue(response.body().contains("Тіло запиту завелике"), response.body());
    }

    @Test
    void chunkedFormUnderLimitIsStillValidated() throws Exception {
        String form = "login=abc&firstName=%D0%9E%D0%BB%D0%B5%D0%BD%D0%B0&email=o%40example.com"
                + "&password=Secret123&phone=%2B380501234567&birthDate=1998-04-12";
        HttpResponse<String> response = postChunked("/register", "application/x-www-form-urlencoded", form);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Логін має містити від 5 до 32 символів"), response.body());
        assertTrue(response.body().contains("value=\"Олена\""), "ім'я з тіла форми має дійти до сервера");
    }
}
