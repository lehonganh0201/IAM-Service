package com.example.iamservice.aop.annotation;

import com.example.iamservice.constant.AuditAction;
import com.example.iamservice.constant.AuditResourceType;

import java.lang.annotation.*;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 8:49
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditActivity {
    AuditAction action();

    AuditResourceType resourceType();

    String message() default "";

    String resourceIdParam() default "";
}
