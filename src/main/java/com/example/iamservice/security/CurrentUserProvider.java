package com.example.iamservice.security;

import com.example.iamservice.exception.UnauthorizedException;
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
            throw new UnauthorizedException("Unauthorized");
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
}
