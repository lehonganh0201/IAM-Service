package com.example.iamservice.aop.aspect;

import com.example.iamservice.aop.annotation.RateLimit;
import com.example.iamservice.config.RateLimitConfig;
import com.example.iamservice.exception.TooManyRequestsException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 13:32
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class RateLimitAspect {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\"email\"\\s*:\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private final ProxyManager<String> proxyManager;
    private final RateLimitConfig rateLimitConfig;

    @Around(value = "com.example.iamservice.aop.aspect.CommonJoinPointConfig.rateLimitPointcut(rateLimit)", argNames = "joinPoint,rateLimit")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        String key = generateKey(rateLimit);

        Supplier<BucketConfiguration> configSupplier = () -> rateLimitConfig.createBucketConfig(
                rateLimit.capacity(),
                rateLimit.refillAmount(),
                rateLimit.refillDurationMinutes());

        Bucket bucket = proxyManager.builder()
                .build(key, configSupplier);

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        }

        log.warn("Rate limit exceeded for key: {}", key);
        throw new TooManyRequestsException("Too many requests. Please try again later.");
    }

    private String generateKey(RateLimit rateLimit) {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        String baseKey = rateLimit.key().isEmpty() ? "rl" : rateLimit.key();

        return switch (rateLimit.strategy()) {
            case "USER" -> baseKey + ":user:" + getCurrentUserId();
            case "EMAIL" -> baseKey + ":email:" + getEmailFromRequest(request);
            case "COMBINED" -> baseKey + ":combined:" + getClientIP(request) + ":" + getCurrentUserId();
            default -> baseKey + ":ip:" + getClientIP(request);
        };
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }

    private String getCurrentUserId() {
        try {
            var authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception ignored) {}
        return "anonymous";
    }

    private String getEmailFromRequest(HttpServletRequest request) {
        String email = request.getParameter("email");
        if (email != null && !email.trim().isEmpty()) {
            return email.toLowerCase().trim();
        }

        try {
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                Matcher matcher = EMAIL_PATTERN.matcher(body);
                if (matcher.find()) {
                    email = matcher.group(1);
                    return email.toLowerCase().trim();
                }
            }
        } catch (Exception e) {
            log.debug("Cannot read request body for rate limit: {}", e.getMessage());
        }

        return "unknown";
    }

    private String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            }
            return StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.debug("Failed to read request body", e);
            return null;
        }
    }
}