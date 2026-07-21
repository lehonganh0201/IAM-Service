package com.example.iamservice.security.jwt;

import com.example.commonlib.api.ApiError;
import com.example.commonlib.api.ApiResponse;
import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.constant.AuditResult;
import com.example.iamservice.domain.dto.request.AuditLogCommand;
import com.example.iamservice.service.AuditLogService;
import com.example.iamservice.util.AuditRequestInfoProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:02
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;
    private final AuditRequestInfoProvider auditRequestInfoProvider;

    @Override
    public void commence(@NonNull HttpServletRequest request,
                         @NonNull HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String requestId = MDC.get("requestId");

        ApiResponse<Void> body = new ApiResponse<>(
                false,
                "Unauthorized",
                null,
                ApiError.of("Authorization"),
                Instant.now(),
                request.getRequestURI(),
                requestId
        );

        auditLogService.save(
                AuditLogCommand.builder()
                        .actorUsername("anonymous")
                        .action(AuditAction.UNAUTHORIZED)
                        .resourceType(AuditResourceType.SECURITY)
                        .result(AuditResult.FAILURE)
                        .message("Unauthorized request")
                        .errorMessage(authException.getMessage())
                        .httpMethod(auditRequestInfoProvider.method())
                        .requestPath(auditRequestInfoProvider.path())
                        .ipAddress(auditRequestInfoProvider.ipAddress())
                        .userAgent(auditRequestInfoProvider.userAgent())
                        .requestId(auditRequestInfoProvider.requestId())
                        .build()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
