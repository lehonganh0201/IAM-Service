package com.example.iamservice.service.impl;

import com.example.iamservice.domain.dto.response.AuditLogResponse;
import com.example.iamservice.domain.dto.response.common.PageResponse;
import com.example.iamservice.domain.entity.AuditLog;
import com.example.iamservice.domain.mapper.AuditLogMapper;
import com.example.iamservice.repository.AuditLogRepository;
import com.example.iamservice.repository.specification.AuditLogSpecification;
import com.example.iamservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
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
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

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
}