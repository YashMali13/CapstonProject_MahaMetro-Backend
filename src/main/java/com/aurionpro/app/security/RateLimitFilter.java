package com.aurionpro.app.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${rate-limit.default.capacity}")
    private int defaultCapacity;
    @Value("${rate-limit.default.refill-rate}")
    private int defaultRefillRate;
    @Value("${rate-limit.default.refill-period-seconds}")
    private int defaultRefillPeriod;

    @Value("${rate-limit.auth.capacity}")
    private int authCapacity;
    @Value("${rate-limit.auth.refill-rate}")
    private int authRefillRate;
    @Value("${rate-limit.auth.refill-period-seconds}")
    private int authRefillPeriod;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        currentRequestPath.set(request.getRequestURI());
        try {
            String key = resolveKey(request);
            Bucket bucket = this.cache.computeIfAbsent(key, this::createNewBucket);

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                log.warn("Rate limit exceeded for key: {} on path {}", key, request.getRequestURI());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(
                    Map.of("error", "Rate limit exceeded. Try again later.")
                ));
            }
        } finally {
            currentRequestPath.remove();
        }
    }
    private String resolveKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                return jwtService.extractEmail(token);
            } catch (Exception e) {
                return request.getRemoteAddr();
            }
        }
        return request.getRemoteAddr();
    }

    private Bucket createNewBucket(String key) {
        String path = currentRequestPath.get();
        if (path.startsWith("/api/v1/auth")) {
            return Bucket.builder().addLimit(getAuthPolicy()).build();
        }
        return Bucket.builder().addLimit(getDefaultPolicy()).build();
    }
    
    private static final ThreadLocal<String> currentRequestPath = new ThreadLocal<>();
    
    private Bandwidth getAuthPolicy() {
        return Bandwidth.classic(authCapacity, Refill.greedy(authRefillRate, Duration.ofSeconds(authRefillPeriod)));
    }
    
    private Bandwidth getDefaultPolicy() {
        return Bandwidth.classic(defaultCapacity, Refill.greedy(defaultRefillRate, Duration.ofSeconds(defaultRefillPeriod)));
    }
}