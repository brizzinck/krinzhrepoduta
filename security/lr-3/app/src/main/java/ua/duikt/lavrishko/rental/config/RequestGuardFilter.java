package ua.duikt.lavrishko.rental.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            tooLarge(response);
            return;
        }
        HttpServletRequest checked = request;
        // Chunked-запит не має Content-Length: читаємо тіло сам, але не більше ліміту
        if (request.getContentLengthLong() < 0 && request.getHeader("Transfer-Encoding") != null) {
            byte[] body = readAtMost(request.getInputStream(), maxBodyBytes);
            if (body == null) {
                tooLarge(response);
                return;
            }
            checked = new BufferedBodyRequest(request, body);
        }
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        // CSP діє скрізь, окрім навмисно вразливих демо-сторінок: вона блокує inline-скрипти навіть при помилці в коді
        if (!request.getRequestURI().startsWith("/demo/vuln")) {
            response.setHeader("Content-Security-Policy",
                    "default-src 'self'; script-src 'self'; style-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'");
        }
        chain.doFilter(checked, response);
    }

    private void tooLarge(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Connection", "close");
        response.getWriter().write("{\"error\":\"Тіло запиту завелике, максимум " + maxBodyBytes + " байтів\"}");
    }

    /** Тіло цілком, якщо воно не довше за limit; null, якщо довше (решту не читаємо). */
    static byte[] readAtMost(InputStream in, long limit) throws IOException {
        byte[] body = in.readNBytes((int) limit + 1);
        return body.length > limit ? null : body;
    }

    /** Запит із уже прочитаним тілом: віддає його як потік, а для форми розбирає параметри. */
    static class BufferedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;
        private Map<String, String[]> params;

        BufferedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream in = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return in.read();
                }

                @Override
                public int read(byte[] b, int off, int len) {
                    return in.read(b, off, len);
                }

                @Override
                public boolean isFinished() {
                    return in.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), charset()));
        }

        @Override
        public String getParameter(String name) {
            String[] values = getParameterMap().get(name);
            return values == null ? null : values[0];
        }

        @Override
        public String[] getParameterValues(String name) {
            return getParameterMap().get(name);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(getParameterMap().keySet());
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            if (params == null) {
                // Тіло вже прочитане, тому сервер дає лише параметри з рядка запиту; поля форми додаємо самі
                Map<String, List<String>> all = new LinkedHashMap<>();
                super.getParameterMap().forEach((k, v) -> all.computeIfAbsent(k, x -> new ArrayList<>()).addAll(List.of(v)));
                String type = getContentType();
                if (type != null && type.startsWith("application/x-www-form-urlencoded")) {
                    for (String pair : new String(body, charset()).split("&")) {
                        int eq = pair.indexOf('=');
                        try {
                            String key = URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq), charset());
                            String value = eq < 0 ? "" : URLDecoder.decode(pair.substring(eq + 1), charset());
                            if (!key.isEmpty()) {
                                all.computeIfAbsent(key, x -> new ArrayList<>()).add(value);
                            }
                        } catch (IllegalArgumentException badEncoding) {
                            // некоректне %-кодування: пару пропускаємо, як і Tomcat
                        }
                    }
                }
                Map<String, String[]> result = new LinkedHashMap<>();
                all.forEach((k, v) -> result.put(k, v.toArray(new String[0])));
                params = Collections.unmodifiableMap(result);
            }
            return params;
        }

        private Charset charset() {
            String enc = getCharacterEncoding();
            return enc == null ? StandardCharsets.UTF_8 : Charset.forName(enc);
        }
    }
}
