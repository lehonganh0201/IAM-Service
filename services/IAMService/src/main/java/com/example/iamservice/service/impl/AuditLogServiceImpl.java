package com.example.iamservice.service.impl;

import com.example.commonlib.api.common.PageResponse;
import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.constant.AuditResult;
import com.example.iamservice.domain.dto.request.AuditLogCommand;
import com.example.iamservice.domain.dto.response.AuditLogResponse;
import com.example.iamservice.domain.entity.AuditLog;
import com.example.iamservice.domain.mapper.AuditLogMapper;
import com.example.iamservice.repository.AuditLogRepository;
import com.example.iamservice.repository.specification.AuditLogSpecification;
import com.example.iamservice.service.AuditLogService;
import com.example.iamservice.util.AuditRequestInfoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 9:29
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final AuditRequestInfoProvider auditRequestInfoProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(
            Long actorUserId,
            String action,
            String result,
            String keyword,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        Specification<AuditLog> specification = Specification
                .where(AuditLogSpecification.actorUserIdEquals(actorUserId))
                .and(AuditLogSpecification.actionEquals(action))
                .and(AuditLogSpecification.resultEquals(result))
                .and(AuditLogSpecification.keywordContains(keyword))
                .and(AuditLogSpecification.createdAtFrom(from))
                .and(AuditLogSpecification.createdAtTo(to));

        Page<AuditLog> page = auditLogRepository.findAll(specification, pageable);

        return PageResponse.from(page.map(auditLogMapper::toResponse));
    }

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