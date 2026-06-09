package com.example.iamservice.aop.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 10:11
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Aspect
@Configuration
@Log4j2
public class ControllerAspect {

    private static final String UNKNOWN = "UNKNOWN";

    @Around("com.example.iamservice.aop.aspect.CommonJoinPointConfig.controllerPointcut()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        HttpServletResponse response = attributes != null ? attributes.getResponse() : null;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String controllerMethod = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String method = request != null ? request.getMethod() : UNKNOWN;
        String uri = request != null ? request.getRequestURI() : UNKNOWN;

        try {
            Object result = joinPoint.proceed();
            int status = response != null ? response.getStatus() : HttpStatus.OK.value();

            log.info("API SUCCESS | method={} | request={} | controller={} | status={} | duration={}ms",
                    method, uri, controllerMethod, status, System.currentTimeMillis() - startTime);

            return result;

        } catch (Exception ex) {
            int status = response != null ? response.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR.value();
            log.error("API ERROR | method={} | request={} | controller={} | status={} | duration={}ms | error={}",
                    method, uri, controllerMethod, status, System.currentTimeMillis() - startTime, ex.getMessage(), ex);
            throw ex;
        }
    }
}
