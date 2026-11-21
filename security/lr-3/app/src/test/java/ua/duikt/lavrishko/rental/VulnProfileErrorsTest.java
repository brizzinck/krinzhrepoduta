package ua.duikt.lavrishko.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Профіль vuln: подробиці помилок розкриває лише вразлива демо-сторінка, а безпечні ендпоінти
 * і тут повертають загальні відповіді. Запити йдуть через справжній Tomcat, бо MockMvc не будує сторінку /error.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("vuln")
class VulnProfileErrorsTest {
    @LocalServerPort
    int port;

    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private HttpResponse<String> send(String method, String path, String contentType, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (contentType != null) {
            request.header("Content-Type", contentType);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertGeneric(HttpResponse<String> response, int status) {
        assertEquals(status, response.statusCode(), response.body());
        assertFalse(response.body().contains("trace"), response.body());
        assertFalse(response.body().contains("exception"), response.body());
        assertFalse(response.body().contains("springframework"), response.body());
    }

    @Test
    void safeApiHasGenericErrorOnUnsupportedMediaType() throws Exception {
        assertGeneric(send("POST", "/api/register", "text/plain", "x"), 415);
    }

    @Test
    void safeApiHasGenericErrorOnWrongMethod() throws Exception {
        assertGeneric(send("DELETE", "/api/register", null, null), 405);
    }

    @Test
    void safeFormHasGenericErrorOnWrongMethod() throws Exception {
        assertGeneric(send("PUT", "/register", null, null), 405);
    }

    @Test
    void vulnerablePageStillLeaksTrace() throws Exception {
        HttpResponse<String> response = send("GET", "/demo/vuln/page?n=abc", null, null);
        assertEquals(500, response.statusCode());
        assertTrue(response.body().contains("\"exception\":\"java.lang.NumberFormatException\""), response.body());
        assertTrue(response.body().contains("\"trace\""), response.body());
    }

    @Test
    void vulnerableSearchLeaksSqlError() throws Exception {
        HttpResponse<String> response = send("GET", "/demo/vuln/search?brand=%27", null, null);
        assertEquals(500, response.statusCode());
        assertTrue(response.body().contains("BadSqlGrammarException"), response.body());
        assertTrue(response.body().contains("\"trace\""), response.body());
    }
}
