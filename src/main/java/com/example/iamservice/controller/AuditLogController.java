package com.example.iamservice.controller;

import com.example.iamservice.aop.annotation.AuditActivity;
import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.domain.dto.response.AuditLogResponse;
import com.example.iamservice.domain.dto.response.common.ApiResponse;
import com.example.iamservice.domain.dto.response.common.ApiResponseFactory;
import com.example.iamservice.domain.dto.response.common.PageResponse;
import com.example.iamservice.domain.dto.response.common.PageableFactory;
import com.example.iamservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 9:30
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private static final Set<String> AUDIT_SORT_FIELDS = Set.of(
            "id",
            "actorUserId",
            "action",
            "result",
            "createdAt"
    );

    private final AuditLogService auditLogQueryService;
    private final ApiResponseFactory responseFactory;
    private final PageableFactory pageableFactory;

    @AuditActivity(
            action = AuditAction.AUDIT_LOG_READ,
            resourceType = AuditResourceType.AUDIT_LOG,
            message = "Read audit logs"
    )
    @PreAuthorize("hasPermission(null, 'AUDIT_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Pageable pageable = pageableFactory.create(
                page,
                size,
                sortBy,
                sortDir,
                AUDIT_SORT_FIELDS
        );

        PageResponse<AuditLogResponse> data = auditLogQueryService.getAuditLogs(
                actorUserId,
                action,
                result,
                keyword,
                from,
                to,
                pageable
        );

        return ResponseEntity.ok(
                responseFactory.success("Get audit logs successfully", data)
        );
    }
}
