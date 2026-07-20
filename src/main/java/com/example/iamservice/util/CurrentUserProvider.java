package com.example.iamservice.util;

import com.example.iamservice.exception.UnauthorizedException;
import com.example.iamservice.security.IamPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 17:04
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class CurrentUserProvider {
    public Long getCurrentUserId() {
        Long userId = getCurrentUserIdOrNull();

        if (userId == null) {
            throw new UnauthorizedException("Unauthorized when get current user id");
        }

        return userId;
    }


    public Long getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof IamPrincipal iamPrincipal) {
            return iamPrincipal.userId();
        }

        return null;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Unauthorized when get current username");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof IamPrincipal iamPrincipal) {
            return iamPrincipal.username();
        }

        return null;
    }
}
