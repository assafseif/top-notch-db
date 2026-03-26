package com.project.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.ApiErrorResponse;
import jakarta.annotation.PostConstruct;
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
import java.util.concurrent.Semaphore;

@Component
public class BulkheadFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private Semaphore semaphore;

    @Value("${app.security.bulkhead.max-concurrent-requests:100}")
    private int maxConcurrentRequests;

    public BulkheadFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void initializeSemaphore() {
        semaphore = new Semaphore(Math.max(1, maxConcurrentRequests), true);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!semaphore.tryAcquire()) {
            writeError(response, HttpStatus.SERVICE_UNAVAILABLE, "The server is handling too many concurrent requests. Please try again shortly.");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            semaphore.release();
        }
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(status.value(), message));
    }
}