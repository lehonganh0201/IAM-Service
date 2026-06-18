package com.example.iamservice.service.impl;

import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.constant.AuditResult;
import com.example.iamservice.domain.dto.request.AuditLogCommand;
import com.example.iamservice.domain.entity.AuditLog;
import com.example.iamservice.repository.AuditLogRepository;
import com.example.iamservice.util.AuditRequestInfoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 8:47
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditRequestInfoProvider auditRequestInfoProvider;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AuditLogCommand command) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .actorUserId(command.actorUserId())
                    .actorUsername(command.actorUsername())
                    .actorEmail(command.actorEmail())
                    .identityProvider(command.identityProvider())
                    .action(command.action())
                    .resourceType(command.resourceType())
                    .resourceId(command.resourceId())
                    .result(command.result())
                    .message(command.message())
                    .errorMessage(limit(command.errorMessage(), 1000))
                    .httpMethod(command.httpMethod())
                    .requestPath(command.requestPath())
                    .ipAddress(command.ipAddress())
                    .userAgent(limit(command.userAgent(), 500))
                    .requestId(command.requestId())
                    .createdAt(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception exception) {
            log.warn("Failed to save audit log: {}", exception.getMessage());
        }
    }

    public void saveAuthAudit(
            AuditAction action,
            AuditResult result,
            String usernameOrEmail,
            Long actorUserId,
            String errorMessage
    ) {
        auditLogRepository.save(
                AuditLog.builder()
                        .actorUserId(actorUserId)
                        .actorUsername(usernameOrEmail)
                        .action(action)
                        .resourceType(AuditResourceType.AUTH)
                        .result(result)
                        .message(action.name())
                        .errorMessage(limit(errorMessage, 1000))
                        .httpMethod(auditRequestInfoProvider.method())
                        .requestPath(auditRequestInfoProvider.path())
                        .ipAddress(auditRequestInfoProvider.ipAddress())
                        .userAgent(limit(auditRequestInfoProvider.userAgent(), 500))
                        .requestId(auditRequestInfoProvider.requestId())
                        .build()
        );
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
