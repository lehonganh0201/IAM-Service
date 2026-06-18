package com.example.iamservice.service;

import com.example.iamservice.domain.dto.response.AuditLogResponse;
import com.example.iamservice.domain.dto.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 9:28
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface AuditLogService {
    PageResponse<AuditLogResponse> getAuditLogs(
            Long actorUserId,
            String action,
            String result,
            String keyword,
            Instant from,
            Instant to,
            Pageable pageable
    );
}
