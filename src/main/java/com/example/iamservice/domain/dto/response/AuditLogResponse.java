package com.example.iamservice.domain.dto.response;

import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.constant.AuditResult;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 9:26
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actorUsername,
        String actorEmail,
        String identityProvider,
        AuditAction action,
        AuditResourceType resourceType,
        String resourceId,
        AuditResult result,
        String message,
        String errorMessage,
        String httpMethod,
        String requestPath,
        String ipAddress,
        String userAgent,
        String requestId,
        Instant createdAt
) {
}
