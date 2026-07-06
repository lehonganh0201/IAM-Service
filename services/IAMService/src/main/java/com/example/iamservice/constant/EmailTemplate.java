package com.example.iamservice.constant;

import com.example.iamservice.exception.NotFoundException;
import lombok.Getter;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 10:18
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


@Getter
public enum EmailTemplate {

    REGISTRATION_CONFIRMATION(
            "email/registration-confirmation",
            "Chào mừng bạn đến với IAM Service - Xác nhận tài khoản",
            "registration"
    ),

    ACCOUNT_VERIFICATION(
            "email/account-verification",
            "Xác thực địa chỉ email của bạn",
            "verification"
    ),

    PASSWORD_RESET(
            "email/password-reset",
            "Hướng dẫn đặt lại mật khẩu của bạn",
            "reset-password"
    ),

    PASSWORD_CHANGED(
            "email/password-changed",
            "Thông báo: Mật khẩu tài khoản đã được thay đổi",
            "security-alert"
    ),

    PROFILE_UPDATED(
            "email/profile-updated",
            "Thông tin tài khoản của bạn đã được cập nhật",
            "profile-update"
    ),

    TWO_FACTOR_OTP(
            "email/two-factor-otp",
            "Mã xác thực hai lớp (OTP) - IAM Service",
            "otp"
    );

    private final String templatePath;
    private final String subject;
    private final String templateType;

    EmailTemplate(String templatePath, String subject, String templateType) {
        this.templatePath = templatePath;
        this.subject = subject;
        this.templateType = templateType;
    }

    /**
     * Tìm template theo tên (hữu ích khi config động)
     */
    public static EmailTemplate fromName(String name) {
        for (EmailTemplate template : values()) {
            if (template.name().equalsIgnoreCase(name)) {
                return template;
            }
        }
        throw new NotFoundException("Email template not found: " + name);
    }
}
