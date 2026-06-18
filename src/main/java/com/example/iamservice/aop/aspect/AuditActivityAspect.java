package com.example.iamservice.aop.aspect;

import com.example.iamservice.aop.annotation.AuditActivity;
import com.example.iamservice.constant.AuditResult;
import com.example.iamservice.domain.dto.request.AuditLogCommand;
import com.example.iamservice.service.impl.AuditLogService;
import com.example.iamservice.util.AuditActorProvider;
import com.example.iamservice.util.AuditRequestInfoProvider;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 8:51
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Aspect
@Component
@RequiredArgsConstructor
public class AuditActivityAspect {

    private final AuditLogService auditLogService;
    private final AuditActorProvider auditActorProvider;
    private final AuditRequestInfoProvider requestInfoProvider;

    @Around(value = "com.example.iamservice.aop.aspect.CommonJoinPointConfig.auditActivityPointcut(auditActivity)", argNames = "joinPoint, auditActivity")
    public Object audit(ProceedingJoinPoint joinPoint, AuditActivity auditActivity) throws Throwable {
        AuditActorProvider.Actor actor =
                auditActorProvider.currentActorOrAnonymous();

        String resourceId = resolveResourceId(joinPoint, auditActivity.resourceIdParam());

        try {
            Object result = joinPoint.proceed();

            saveAuditLog(
                    actor,
                    auditActivity,
                    resourceId,
                    AuditResult.SUCCESS,
                    null
            );

            return result;
        } catch (Throwable throwable) {
            saveAuditLog(
                    actor,
                    auditActivity,
                    resourceId,
                    AuditResult.FAILURE,
                    throwable.getMessage()
            );

            throw throwable;
        }
    }

    private void saveAuditLog(
            AuditActorProvider.Actor actor,
            AuditActivity annotation,
            String resourceId,
            AuditResult result,
            String errorMessage
    ) {
        auditLogService.save(
                AuditLogCommand.builder()
                        .actorUserId(actor.userId())
                        .actorUsername(actor.username())
                        .actorEmail(actor.email())
                        .identityProvider(actor.identityProvider())
                        .action(annotation.action())
                        .resourceType(annotation.resourceType())
                        .resourceId(resourceId)
                        .result(result)
                        .message(annotation.message())
                        .errorMessage(errorMessage)
                        .httpMethod(requestInfoProvider.method())
                        .requestPath(requestInfoProvider.path())
                        .ipAddress(requestInfoProvider.ipAddress())
                        .userAgent(requestInfoProvider.userAgent())
                        .requestId(requestInfoProvider.requestId())
                        .build()
        );
    }

    private String resolveResourceId(
            ProceedingJoinPoint joinPoint,
            String resourceIdParam
    ) {
        if (resourceIdParam == null || resourceIdParam.isBlank()) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if (resourceIdParam.equals(parameterNames[i])) {
                Object value = args[i];
                return value == null ? null : String.valueOf(value);
            }
        }

        return null;
    }
}
