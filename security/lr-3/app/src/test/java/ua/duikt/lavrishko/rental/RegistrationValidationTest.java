package ua.duikt.lavrishko.rental;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Серверна валідація форми реєстрації: кожне правило з методички, валідні й невалідні значення. */
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationValidationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanUsers() {
        jdbc.update("DELETE FROM users WHERE login <> 'manager'");
    }

    /** Коректний набір полів; кожен тест змінює лише одне поле. */
    private static MockHttpServletRequestBuilder form(Map<String, String> overrides) {
        Map<String, String> f = new java.util.LinkedHashMap<>(Map.of(
                "login", "client01",
                "firstName", "Олена",
                "email", "olena@example.com",
                "password", "Secret123",
                "phone", "+380501234567",
                "birthDate", "1998-04-12"));
        f.putAll(overrides);
        MockHttpServletRequestBuilder req = post("/register").contentType(MediaType.APPLICATION_FORM_URLENCODED);
        f.forEach(req::param);
        return req;
    }

    @Test
    void validDataIsAcceptedAndPasswordStoredAsBcrypt() throws Exception {
        mvc.perform(form(Map.of())).andExpect(status().is3xxRedirection());
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT login, password_hash, role FROM users WHERE login = 'client01'");
        assertEquals(1, rows.size());
        String hash = (String) rows.get(0).get("password_hash");
        assertTrue(hash.startsWith("$2a$10$"), "очікується BCrypt-хеш, а не пароль: " + hash);
        assertEquals("CLIENT", rows.get(0).get("role"));
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
                // логін: мінімум 5 символів + allow-list
                Arguments.of("login", "", "Вкажіть логін"),
                Arguments.of("login", "abcd", "від 5 до 32"),
                Arguments.of("login", "a".repeat(33), "від 5 до 32"),
                Arguments.of("login", "user name", "лише латинські літери"),
                Arguments.of("login", "юзер_кириличний", "лише латинські літери"),
                Arguments.of("login", "admin'--", "лише латинські літери"),
                Arguments.of("login", "client\u0000x", "лише латинські літери"),
                Arguments.of("login", "<script>alert(1)</script>", "лише латинські літери"),
                // ім'я: лише літери
                Arguments.of("firstName", "", "Вкажіть ім"),
                Arguments.of("firstName", "Олена2", "лише літери"),
                Arguments.of("firstName", "Anna Maria", "лише літери"),
                Arguments.of("firstName", "<b>Олена</b>", "лише літери"),
                Arguments.of("firstName", "Олена_", "лише літери"),
                Arguments.of("firstName", "Ж".repeat(51), "довшим за 50"),
                // пошта: @ і доменне ім'я
                Arguments.of("email", "", "Вкажіть e-mail"),
                Arguments.of("email", "olena.example.com", "@ та доменне"),
                Arguments.of("email", "olena@", "@ та доменне"),
                Arguments.of("email", "a@b", "@ та доменне"),
                Arguments.of("email", "a@b..com", "@ та доменне"),
                Arguments.of("email", "a b@example.com", "@ та доменне"),
                Arguments.of("email", "a".repeat(250) + "@e.com", "довшим за 254"),
                // пароль: >= 8, велика літера, цифра
                Arguments.of("password", "", "Вкажіть пароль"),
                Arguments.of("password", "Abc123", "від 8 до 72"),
                Arguments.of("password", "secret123", "велику літеру"),
                Arguments.of("password", "SecretPass", "цифру"),
                Arguments.of("password", "Aa1".repeat(30), "від 8 до 72"),
                Arguments.of("password", "Ж1".repeat(30), "72 байтів"),
                // телефон
                Arguments.of("phone", "", "Вкажіть номер"),
                Arguments.of("phone", "abc", "від 10 до 15 цифр"),
                Arguments.of("phone", "+38 050 123 45 67", "від 10 до 15 цифр"),
                Arguments.of("phone", "12345", "від 10 до 15 цифр"),
                Arguments.of("phone", "1".repeat(40), "від 10 до 15 цифр"),
                // дата народження: формат, календар, майбутнє, діапазон
                Arguments.of("birthDate", "", "Вкажіть дату"),
                Arguments.of("birthDate", "abc", "Некоректна дата"),
                Arguments.of("birthDate", "2026-02-31", "Некоректна дата"),
                Arguments.of("birthDate", "12.04.1998", "Некоректна дата"),
                Arguments.of("birthDate", "99999999999999999999", "Некоректна дата"),
                Arguments.of("birthDate", "2099-01-01", "в майбутньому"),
                Arguments.of("birthDate", "1850-01-01", "раніше 1900"),
                Arguments.of("birthDate", LocalDate.now().minusYears(10).toString(), "з 18 років"));
    }

    @ParameterizedTest(name = "{0} = «{1}» відхиляється")
    @MethodSource("invalidCases")
    void invalidFieldIsRejectedWithMessage(String field, String value, String expectedMessage) throws Exception {
        mvc.perform(form(Map.of(field, value)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedMessage)));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE login <> 'manager'", Integer.class),
                "при помилці валідації запис у БД не створюється");
    }

    @Test
    void missingFieldsAreRejectedWithoutBrowser() throws Exception {
        // запит без жодного поля: так поводиться клієнт, що обійшов JavaScript
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Вкажіть логін")))
                .andExpect(content().string(containsString("Вкажіть пароль")));
    }

    @Test
    void emptyFieldDoesNotHideFormatErrorsOfOtherFields() throws Exception {
        // порожній логін + некоректні решта полів: у відповіді мають бути помилки всіх полів
        mvc.perform(form(Map.of("login", "", "firstName", "Ol3na", "email", "olena@example",
                        "password", "secret", "phone", "abc", "birthDate", "2099-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Вкажіть логін")))
                .andExpect(content().string(not(containsString("від 5 до 32"))))
                .andExpect(content().string(containsString("лише літери")))
                .andExpect(content().string(containsString("@ та доменне")))
                .andExpect(content().string(containsString("велику літеру")))
                .andExpect(content().string(containsString("від 10 до 15 цифр")))
                .andExpect(content().string(containsString("в майбутньому")));
    }

    @Test
    void duplicatedParameterIsRejected() throws Exception {
        // name=a&name=b зводиться до «a,b»: кома не літера
        mvc.perform(form(Map.of()).param("firstName", "Іван"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("лише літери")));
    }

    @Test
    void duplicateLoginIsRejected() throws Exception {
        mvc.perform(form(Map.of())).andExpect(status().is3xxRedirection());
        mvc.perform(form(Map.of("email", "other@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("уже зайнятий")));
    }

    @Test
    void roleParameterIsIgnoredMassAssignment() throws Exception {
        mvc.perform(form(Map.of()).param("role", "ADMIN")).andExpect(status().is3xxRedirection());
        assertEquals("CLIENT", jdbc.queryForObject("SELECT role FROM users WHERE login = 'client01'", String.class));
    }

    @Test
    void hugeBodyIsRejectedWith413() throws Exception {
        byte[] body = ("login=" + "a".repeat(5_000_000)).getBytes(StandardCharsets.UTF_8);
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_FORM_URLENCODED).content(body))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void formIsRenderedWithoutRawErrors() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Реєстрація користувача")))
                .andExpect(content().string(not(containsString("Exception"))));
    }
}
