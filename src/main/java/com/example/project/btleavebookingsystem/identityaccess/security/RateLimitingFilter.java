package com.example.project.btleavebookingsystem.identityaccess.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window rate limiting per client IP, in two tiers:
 *  - POST /api/auth/login: a tight limit to slow credential brute-forcing
 *  - everything else: a generous limit to protect the API from abuse
 * Exceeding a limit returns 429 with a JSON body and a Retry-After header, and is logged.
 * Limits are configurable (rate-limit.* properties).
 *
 * Known limitations (evaluated in the report): state is in memory and per instance (a shared
 * store such as Redis/Bucket4j would be needed behind a load balancer), fixed windows allow
 * short bursts at window boundaries, and the client IP is the direct peer address.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final String LOGIN_PATH = "/api/auth/login";

    private final int loginMaxRequests;
    private final int apiMaxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitingFilter(@Value("${rate-limit.login.max-requests:30}") int loginMaxRequests,
                              @Value("${rate-limit.api.max-requests:300}") int apiMaxRequests,
                              @Value("${rate-limit.window-ms:60000}") long windowMs) {
        this.loginMaxRequests = loginMaxRequests;
        this.apiMaxRequests = apiMaxRequests;
        this.windowMs = windowMs;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        boolean isLogin = "POST".equalsIgnoreCase(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI());
        String clientIp = request.getRemoteAddr();
        String bucketKey = (isLogin ? "login:" : "api:") + clientIp;
        int limit = isLogin ? loginMaxRequests : apiMaxRequests;

        Window window = windows.computeIfAbsent(bucketKey, key -> new Window());
        if (window.isExpired(windowMs)) {
            window.reset();
        }

        if (window.count.incrementAndGet() > limit) {
            long retryAfterSeconds = window.secondsUntilReset(windowMs);
            log.warn("Rate limit exceeded: {} {} from {} ({} tier, limit {} per {} ms)",
                    request.getMethod(), request.getRequestURI(), clientIp,
                    isLogin ? "login" : "api", limit, windowMs);
            writeTooManyRequests(response, retryAfterSeconds);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 429);
        body.put("error", "Too Many Requests");
        body.put("message", "Rate limit exceeded - retry after " + retryAfterSeconds + " seconds");

        objectMapper.writeValue(response.getWriter(), body);
    }

    private static class Window {
        private volatile long windowStart = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger(0);

        boolean isExpired(long windowMs) {
            return System.currentTimeMillis() - windowStart > windowMs;
        }

        synchronized void reset() {
            windowStart = System.currentTimeMillis();
            count.set(0);
        }

        long secondsUntilReset(long windowMs) {
            long remainingMs = windowMs - (System.currentTimeMillis() - windowStart);
            return Math.max(1, (remainingMs + 999) / 1000);
        }
    }
}