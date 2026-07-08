package com.example.commonlib.web;

import com.example.commonlib.web.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final int MAX_BODY_LENGTH = 2000;

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "oldPassword",
            "newPassword",
            "confirmPassword",
            "accessToken",
            "refreshToken",
            "access_token",
            "refresh_token",
            "client_secret",
            "token",
            "authorization"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = getOrCreateRequestId(request);

        MDC.put(RequestContext.REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_BODY_LENGTH);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            long durationMs = System.currentTimeMillis() - startTime;

            log.info(
                    "http_request method={} uri={} query={} status={} durationMs={} requestBody={} responseBody={} requestId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    sanitizeQuery(request.getQueryString()),
                    wrappedResponse.getStatus(),
                    durationMs,
                    sanitizeBody(getRequestBody(wrappedRequest)),
                    sanitizeBody(getResponseBody(wrappedResponse)),
                    requestId
            );

        } catch (Exception ex) {
            long durationMs = System.currentTimeMillis() - startTime;

            log.error(
                    "http_exception method={} uri={} query={} status={} durationMs={} requestBody={} requestId={} error={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    sanitizeQuery(request.getQueryString()),
                    wrappedResponse.getStatus(),
                    durationMs,
                    sanitizeBody(getRequestBody(wrappedRequest)),
                    requestId,
                    ex.getMessage(),
                    ex
            );

            throw ex;

        } finally {
            wrappedResponse.copyBodyToResponse();
            MDC.remove(RequestContext.REQUEST_ID_KEY);
        }
    }

    private String getOrCreateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);

        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }

        return requestId;
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        if (!isReadableContentType(request.getContentType())) {
            return "";
        }

        byte[] content = request.getContentAsByteArray();

        if (content.length == 0) {
            return "";
        }

        return limitLength(new String(content, StandardCharsets.UTF_8));
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        if (!isReadableContentType(response.getContentType())) {
            return "";
        }

        byte[] content = response.getContentAsByteArray();

        if (content.length == 0) {
            return "";
        }

        return limitLength(new String(content, StandardCharsets.UTF_8));
    }

    private boolean isReadableContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        String lowerContentType = contentType.toLowerCase();

        return lowerContentType.contains("application/json")
                || lowerContentType.contains("application/xml")
                || lowerContentType.contains("text/")
                || lowerContentType.contains("application/x-www-form-urlencoded");
    }

    private String sanitizeQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return "";
        }

        String sanitized = query;

        for (String field : SENSITIVE_FIELDS) {
            sanitized = sanitized.replaceAll(
                    "(?i)(^|&)(" + Pattern.quote(field) + ")=[^&]*",
                    "$1$2=***"
            );
        }

        return sanitized;
    }

    private String sanitizeBody(String body) {
        if (!StringUtils.hasText(body)) {
            return "";
        }

        String sanitized = body;

        for (String field : SENSITIVE_FIELDS) {
            sanitized = sanitized.replaceAll(
                    "(?i)(\"" + Pattern.quote(field) + "\"\\s*:\\s*\")[^\"]*(\")",
                    "$1***$2"
            );

            sanitized = sanitized.replaceAll(
                    "(?i)(" + Pattern.quote(field) + "=)[^&\\s]*",
                    "$1***"
            );
        }

        return limitLength(sanitized);
    }

    private String limitLength(String value) {
        if (value == null) {
            return "";
        }

        if (value.length() <= MAX_BODY_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_BODY_LENGTH) + "...[truncated]";
    }
}