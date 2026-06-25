package com.example.iamservice.security.jwt;

import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.constant.AuditResult;
import com.example.iamservice.domain.dto.request.AuditLogCommand;
import com.example.iamservice.domain.dto.response.common.ApiError;
import com.example.iamservice.domain.dto.response.common.ApiResponse;
import com.example.iamservice.service.AuditLogService;
import com.example.iamservice.util.AuditActorProvider;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:08
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;
    private final AuditRequestInfoProvider auditRequestInfoProvider;
    private final AuditActorProvider auditActorProvider;

    @Override
    public void handle(@NonNull HttpServletRequest request,
                       @NonNull HttpServletResponse response,
                       @NonNull AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String requestId = MDC.get("requestId");

        ApiResponse<Void> body = new ApiResponse<>(
                false,
                "Forbidden",
                null,
                ApiError.of("FORBIDDEN"),
                Instant.now(),
                request.getRequestURI(),
                requestId
        );

        AuditActorProvider.Actor actor = auditActorProvider.currentActorOrAnonymous();

        auditLogService.save(
                AuditLogCommand.builder()
                        .actorUserId(actor.userId())
                        .actorUsername(actor.username())
                        .actorEmail(actor.email())
                        .identityProvider(actor.identityProvider())
                        .action(AuditAction.ACCESS_DENIED)
                        .resourceType(AuditResourceType.SECURITY)
                        .result(AuditResult.FAILURE)
                        .message("Access denied")
                        .errorMessage(accessDeniedException.getMessage())
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