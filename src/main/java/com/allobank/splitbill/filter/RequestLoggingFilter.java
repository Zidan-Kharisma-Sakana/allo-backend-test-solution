package com.allobank.splitbill.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String previousId = MDC.get("requestId");
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);
        long started = System.nanoTime();
        try {
            log.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException failure) {
            log.error("Request filter failed", failure);
            throw failure;
        } finally {
            try {
                long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
                log.info("Request completed in {} ms", duration);
            } finally {
                if (previousId == null) {
                    MDC.remove("requestId");
                } else {
                    MDC.put("requestId", previousId);
                }
            }
        }
    }
}
