package com.example.project.btleavebookingsystem.identityaccess.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 60;
    private static final long WINDOW_MS = 60_000;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Window window = windows.computeIfAbsent(request.getRemoteAddr(), key -> new Window());

        if (window.isExpired()) {
            window.reset();
        }

        if (window.count.incrementAndGet() > MAX_REQUESTS_PER_WINDOW) {
            response.setStatus(429);
            response.getWriter().write("Too many requests, slow down.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class Window {
        private volatile long windowStart = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger(0);

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > WINDOW_MS;
        }

        synchronized void reset() {
            windowStart = System.currentTimeMillis();
            count.set(0);
        }
    }
}