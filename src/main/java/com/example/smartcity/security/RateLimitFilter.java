package com.example.smartcity.security;

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // On limite uniquement l’endpoint de déclaration
        if (request.getRequestURI().equals("/api/incidents")
                && request.getMethod().equalsIgnoreCase("POST")) {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String key = (auth != null) ? auth.getName() : request.getRemoteAddr();

            Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                      "message": "Trop de déclarations. Veuillez réessayer dans une minute."
                    }
                """);
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
