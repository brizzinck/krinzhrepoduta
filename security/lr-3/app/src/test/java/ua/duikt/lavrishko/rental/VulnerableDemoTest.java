package ua.duikt.lavrishko.rental;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Контрольні тести: підтверджують, що навмисно вразливі сторінки справді вразливі (профіль vuln). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("vuln")
class VulnerableDemoTest {
    @Autowired
    MockMvc mvc;

    @Test
    void booleanInjectionReturnsAllCars() throws Exception {
        mvc.perform(get("/demo/vuln/search").param("brand", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Corolla")))
                .andExpect(content().string(containsString("X5")));
    }

    @Test
    void unionInjectionLeaksUsers() throws Exception {
        mvc.perform(get("/demo/vuln/search").param("brand",
                        "' UNION SELECT login,password_hash,0,0 FROM users-- "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("$2a$10$hashforlab")));
    }

    @Test
    void xssIsReflectedUnescaped() throws Exception {
        mvc.perform(get("/demo/vuln/search").param("brand", "<script>alert(1)</script>"))
                .andExpect(content().string(containsString("<script>alert(1)</script>")))
                .andExpect(header().doesNotExist("Content-Security-Policy"));
    }
}
