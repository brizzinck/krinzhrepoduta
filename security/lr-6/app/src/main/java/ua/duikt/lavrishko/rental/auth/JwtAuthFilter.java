package ua.duikt.lavrishko.rental.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

/** Аутентифікація за токеном: усі /api/*, крім входу, вимагають заголовок Authorization: Bearer. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    public static final String CLAIMS = "jwt.claims";
    private static final String PREFIX = "Bearer ";

    private final TokenService tokens;
    private final AuditLog audit;

    public JwtAuthFilter(TokenService tokens, AuditLog audit) {
        this.tokens = tokens;
        this.audit = audit;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Шлях нормалізується так само, як це робить Spring MVC: декодування, вилучення ";x", злиття "//".
        // Інакше /api;x/profile або /%61pi/profile обійшли б фільтр, а контролер їх усе одно обробив би.
        String path = UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        return !path.startsWith("/api/") || path.equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            token = header.substring(PREFIX.length()).trim();
        }
        try {
            Claims claims = tokens.verify(token);
            request.setAttribute(CLAIMS, claims);
            chain.doFilter(request, response);
        } catch (TokenRejected e) {
            audit.tokenRejected(e, request.getRemoteAddr(), request.getRequestURI(), token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
            response.setContentType("application/json;charset=UTF-8");
            response.getOutputStream().write(
                    ("{\"error\":\"invalid_token\",\"reason\":\"" + e.reason() + "\"}").getBytes(StandardCharsets.UTF_8));
        }
    }
}
