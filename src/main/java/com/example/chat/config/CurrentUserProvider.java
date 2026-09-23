package com.example.chat.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentUserProvider {

    private static final String USER_ID_HEADER = "X-User-Id";

    public Long getCurrentUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("No HTTP request context available");
        }
        HttpServletRequest request = attrs.getRequest();
        String headerValue = request.getHeader(USER_ID_HEADER);
        if (headerValue == null || headerValue.isBlank()) {
            throw new IllegalArgumentException("Missing required header: " + USER_ID_HEADER);
        }
        try {
            return Long.parseLong(headerValue.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID in header: " + USER_ID_HEADER);
        }
    }
}
