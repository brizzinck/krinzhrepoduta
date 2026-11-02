package ua.duikt.lavrishko.rental;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** JSON-ендпоінт реєстрації та захищені демонстраційні сторінки (атакуючі рядки не діють). */
@SpringBootTest
@AutoConfigureMockMvc
class ApiAndDemoTest {
    @Autowired
    MockMvc mvc;

    @Test
    void sqlInjectionOnSafeSearchReturnsNothing() throws Exception {
        mvc.perform(get("/demo/safe/search").param("brand", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Нічого не знайдено")))
                .andExpect(content().string(not(containsString("Corolla"))));
    }

    @Test
    void unionInjectionOnSafeSearchDoesNotLeakUsers() throws Exception {
        mvc.perform(get("/demo/safe/search").param("brand",
                        "' UNION SELECT login,password_hash,0,0 FROM users-- "))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("$2a$10$hashforlab"))));
    }

    @Test
    void tooLongSearchStringGives400() throws Exception {
        mvc.perform(get("/demo/safe/search").param("brand", "a".repeat(65)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void xssPayloadIsEscapedOnSafeSearch() throws Exception {
        mvc.perform(get("/demo/safe/search").param("brand", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)"))));
    }

    @Test
    void cspHeaderIsPresentOnSafePages() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void numberOutOfIntRangeGives400Not500() throws Exception {
        // JSON без charset у заголовку клієнти читають як UTF-8, тому й у тесті декодуємо явно
        String body = mvc.perform(get("/demo/safe/page").param("n", "2147483648"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("Некоректний параметр"), body);
        mvc.perform(get("/demo/safe/page").param("n", "abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/demo/safe/page").param("n", "42")).andExpect(status().isOk());
    }

    @Test
    void jsonWithWrongTypesGives400() throws Exception {
        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":12345,\"birthDate\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("Exception"))));
        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void jsonWithInvalidFieldsReturnsFieldMessages() throws Exception {
        String body = "{\"login\":\"abc\",\"firstName\":\"Ol3na\",\"email\":\"a@b\",\"password\":\"short\","
                + "\"phone\":\"abc\",\"birthDate\":\"2099-01-01\"}";
        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"fields\"")))
                .andExpect(content().string(containsString("login")))
                .andExpect(content().string(containsString("birthDate")));
    }

    @Test
    void jsonEmptyFieldDoesNotHideOtherFieldErrors() throws Exception {
        String body = "{\"login\":\"\",\"firstName\":\"Ol3na\",\"email\":\"a@b\",\"password\":\"short\","
                + "\"phone\":\"abc\",\"birthDate\":\"2099-01-01\"}";
        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"login\"")))
                .andExpect(content().string(containsString("\"firstName\"")))
                .andExpect(content().string(containsString("\"email\"")))
                .andExpect(content().string(containsString("\"phone\"")))
                .andExpect(content().string(containsString("\"birthDate\"")));
    }
}
