package com.project.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final Map<String, ClientWindow> windows = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupAt = new AtomicLong(0L);

    @Value("${app.security.rate-limit.requests:120}")
    private int maxRequests;

    @Value("${app.security.rate-limit.window-seconds:60}")
    private long windowSeconds;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;
        String clientKey = resolveClientKey(request);
        ClientWindow clientWindow = windows.computeIfAbsent(clientKey, key -> new ClientWindow());

        if (!clientWindow.tryAcquire(now, windowMillis, Math.max(1, maxRequests))) {
            writeError(response, HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please slow down and try again shortly.");
            cleanupExpiredWindows(now, windowMillis);
            return;
        }

        cleanupExpiredWindows(now, windowMillis);
        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void cleanupExpiredWindows(long now, long windowMillis) {
        long previousCleanup = lastCleanupAt.get();
        if (now - previousCleanup < windowMillis || !lastCleanupAt.compareAndSet(previousCleanup, now)) {
            return;
        }

        windows.entrySet().removeIf(entry -> entry.getValue().isExpired(now, windowMillis));
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(status.value(), message));
    }

    private static final class ClientWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private long lastSeenAt = 0L;

        synchronized boolean tryAcquire(long now, long windowMillis, int maxRequests) {
            prune(now, windowMillis);
            lastSeenAt = now;

            if (timestamps.size() >= maxRequests) {
                return false;
            }

            timestamps.addLast(now);
            return true;
        }

        synchronized boolean isExpired(long now, long windowMillis) {
            prune(now, windowMillis);
            return timestamps.isEmpty() && now - lastSeenAt >= windowMillis;
        }

        private void prune(long now, long windowMillis) {
            long cutoff = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.removeFirst();
            }
        }
    }
}