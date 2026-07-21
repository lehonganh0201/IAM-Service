package com.example.iamservice.security;

import com.example.iamservice.service.cache.PermissionLookupService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 14:32
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class DatabasePermissionEvaluator implements PermissionEvaluator {
    private final PermissionLookupService permissionLookupService;

    @Override
    public boolean hasPermission(@NonNull Authentication authentication,
                                 Object targetDomainObject,
                                 @NonNull Object permission) {
        Long userId = extractUserId(authentication);

        if (userId == null) {
            return false;
        }

        return permissionLookupService.hasPermission(userId, permission.toString());
    }

    @Override
    public boolean hasPermission(@NonNull Authentication authentication,
                                 @NonNull Serializable targetId,
                                 String targetType,
                                 @NonNull Object permission) {
        Long userId = extractUserId(authentication);

        if (userId == null) {
            return false;
        }

        return permissionLookupService.hasPermission(userId, permission.toString());
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof IamPrincipal iamPrincipal) {
            return iamPrincipal.userId();
        }

        return null;
    }
}
