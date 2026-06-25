//package com.example.iamservice.aop.aspect;
//
//import com.example.iamservice.domain.entity.UserActivityLog;
//import com.example.iamservice.repository.UserActivityLogRepository;
//import com.example.iamservice.security.IamPrincipal;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.util.Map;
//
///**
// * ----------------------------------------------------------------------------
// * Author:        Hong Anh
// * Created on:    09/06/2026 at 10:11
// * Project:       IAMService
// * Contact:       https://github.com/lehonganh0201
// * ----------------------------------------------------------------------------
// */
//
//@Aspect
//@Configuration
//@Log4j2
//@RequiredArgsConstructor
//public class ControllerAspect {
//
//    private static final String UNKNOWN = "UNKNOWN";
//    private final UserActivityLogRepository userActivityLogRepository;
//
//    private static final Map<String, String> ACTION_MAP = Map.of(
//            "login", "LOGIN",
//            "logout", "LOGOUT",
//            "updateuserpassword", "CHANGE_PASSWORD",
//            "updateuser", "UPDATE_PROFILE",
//            "update", "UPDATE_PROFILE",
//            "getme", "GET_PROFILE"
//    );
//
//    @Around("com.example.iamservice.aop.aspect.CommonJoinPointConfig.controllerPointcut()")
//    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
//        long startTime = System.currentTimeMillis();
//
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
//        HttpServletResponse response = attributes != null ? attributes.getResponse() : null;
//
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        String controllerMethod = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
//        String method = request != null ? request.getMethod() : UNKNOWN;
//        String uri = request != null ? request.getRequestURI() : UNKNOWN;
//
//        try {
//            Object result = joinPoint.proceed();
//            int status = response != null ? response.getStatus() : HttpStatus.OK.value();
//
//            log.info("API SUCCESS | method={} | request={} | controller={} | status={} | duration={}ms",
//                    method, uri, controllerMethod, status, System.currentTimeMillis() - startTime);
//
//            return result;
//
//        } catch (Exception ex) {
//            int status = response != null ? response.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR.value();
//            log.error("API ERROR | method={} | request={} | controller={} | status={} | duration={}ms | error={}",
//                    method, uri, controllerMethod, status, System.currentTimeMillis() - startTime, ex.getMessage(), ex);
//            throw ex;
//        }
//    }
//
//    @AfterReturning(value = "com.example.iamservice.aop.aspect.CommonJoinPointConfig.userActivityLogController()", returning = "result")
//    public void logAfterSuccess(JoinPoint joinPoint, Object result) {
//        logActivity(joinPoint, "SUCCESS", null);
//    }
//
//    @AfterThrowing(pointcut = "com.example.iamservice.aop.aspect.CommonJoinPointConfig.userActivityLogController()", throwing = "ex")
//    public void logAfterException(JoinPoint joinPoint, Exception ex) {
//        logActivity(joinPoint, "FAILED", ex.getMessage());
//    }
//
//    private void logActivity(JoinPoint joinPoint, String status, String errorMessage) {
//        try {
//            String methodName = joinPoint.getSignature().getName();
//            String className = joinPoint.getTarget().getClass().getSimpleName();
//
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//            Long userId = null;
//            String email = null;
//
//            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
//                if (auth.getPrincipal() instanceof IamPrincipal userDetails) {
//                    userId = userDetails.userId();
//                    email = userDetails.email();
//                }
//            }
//
//            String action = determineAction(className, methodName);
//
//            UserActivityLog logEntity = UserActivityLog.builder()
//                    .userId(userId)
//                    .email(email)
//                    .action(action)
//                    .status(status)
//                    .errorMessage(errorMessage)
//                    .build();
//
//            userActivityLogRepository.save(logEntity);
//
//        } catch (Exception e) {
//            log.warn("Failed to create activity log via AOP", e);
//        }
//    }
//
//    private String determineAction(String className, String methodName) {
//        return ACTION_MAP.getOrDefault(methodName.toLowerCase(), methodName.toUpperCase());
//    }
//}
