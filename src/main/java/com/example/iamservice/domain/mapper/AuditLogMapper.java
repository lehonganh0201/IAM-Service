package com.example.iamservice.domain.mapper;

import com.example.iamservice.domain.dto.response.AuditLogResponse;
import com.example.iamservice.domain.entity.AuditLog;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 9:27
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorUserId(),
                auditLog.getActorUsername(),
                auditLog.getActorEmail(),
                auditLog.getIdentityProvider(),
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getResult(),
                auditLog.getMessage(),
                auditLog.getErrorMessage(),
                auditLog.getHttpMethod(),
                auditLog.getRequestPath(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getRequestId(),
                auditLog.getCreatedAt()
        );
    }
}