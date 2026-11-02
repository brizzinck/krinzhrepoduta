package ua.duikt.lavrishko.rental.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Перший рубіж: відсікає надто великі запити ще до розбору параметрів і додає заголовки безпеки.
 * Це доповнює (а не замінює) валідацію полів у DTO.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestGuardFilter extends OncePerRequestFilter {

    private final long maxBodyBytes;

    public RequestGuardFilter(@Value("${app.max-body-bytes:16384}") long maxBodyBytes) {
        this.maxBodyBytes = maxBodyBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getContentLengthLong() > maxBodyBytes) {
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Connection", "close");
            response.getWriter().write("{\"error\":\"Тіло запиту завелике, максимум " + maxBodyBytes + " байтів\"}");
            return;
        }
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        // CSP діє скрізь, окрім навмисно вразливих демо-сторінок: вона блокує inline-скрипти навіть при помилці в коді
        if (!request.getRequestURI().startsWith("/demo/vuln")) {
            response.setHeader("Content-Security-Policy",
                    "default-src 'self'; script-src 'self'; style-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'");
        }
        chain.doFilter(request, response);
    }
}
