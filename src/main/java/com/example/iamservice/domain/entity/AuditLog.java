package com.example.iamservice.domain.entity;

import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;
import com.example.iamservice.constant.AuditResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 8:38
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actorUserId;

    @Column(length = 150)
    private String actorUsername;

    @Column(length = 150)
    private String actorEmail;

    @Column(length = 50)
    private String identityProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private AuditResourceType resourceType;

    @Column(length = 100)
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditResult result;

    @Column(length = 500)
    private String message;

    @Column(length = 1000)
    private String errorMessage;

    @Column(length = 20)
    private String httpMethod;

    @Column(length = 500)
    private String requestPath;

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 100)
    private String requestId;

    @Column(nullable = false)
    private Instant createdAt;
}
