package com.example.iamservice.util;

import com.example.iamservice.context.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 8:46
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class AuditRequestInfoProvider {
    private final HttpServletRequest request;

    public String method() {
        return request.getMethod();
    }

    public String path() {
        return request.getRequestURI();
    }

    public String ipAddress() {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    public String userAgent() {
        return request.getHeader("User-Agent");
    }

    public String requestId() {
        String requestId = MDC.get(RequestContext.REQUEST_ID_KEY);

        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }

        return request.getHeader("X-Request-Id");
    }
}
