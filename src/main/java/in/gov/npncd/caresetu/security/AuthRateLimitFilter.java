package in.gov.npncd.caresetu.security;

import in.gov.npncd.caresetu.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Simple sliding-window rate limiter for auth endpoints (VAPT). */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private final int capacity;

    public AuthRateLimitFilter(SecurityProperties properties) {
        this.capacity = Math.max(1, properties.rateLimit().authRequestsPerMinute());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Rate-limit only credential endpoints. /auth/me is called on every page load
        // (and twice under React Strict Mode) and must not log users out with 429.
        if (path.startsWith("/api/v1/auth/me")) {
            return true;
        }
        return !(path.startsWith("/api/v1/auth/otp")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/api/v1/auth/logout"));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String key = clientIp(request);
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000L;

        Deque<Long> queue = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            for (Iterator<Long> it = queue.iterator(); it.hasNext(); ) {
                if (it.next() < windowStart) {
                    it.remove();
                } else {
                    break;
                }
            }
            if (queue.size() >= capacity) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Too many requests. Try again later.\",\"code\":\"RATE_LIMITED\"}");
                return;
            }
            queue.addLast(now);
        }
        filterChain.doFilter(request, response);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
