package com.example.iamservice.constant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 8:36
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public enum AuditAction {
    AUTH_LOGIN_SUCCESS,
    AUTH_LOGIN_FAILURE,
    AUTH_LOGOUT,
    AUTH_REFRESH_TOKEN,
    AUTH_REGISTER,

    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    USER_LOCK,
    USER_UNLOCK,
    USER_ASSIGN_ROLE,
    USER_RESET_PASSWORD,

    ROLE_CREATE,
    ROLE_UPDATE,
    ROLE_DELETE,
    ROLE_ASSIGN_PERMISSION,

    PERMISSION_CREATE,
    PERMISSION_UPDATE,
    PERMISSION_DELETE,

    ACCESS_DENIED,
    UNAUTHORIZED,

    AUDIT_LOG_READ
}
