package com.example.commonlib.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:08
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static CurrentUser currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal()))
            return CurrentUser.anonymous();
        String id = a.getName(), u = a.getName();
        if (a instanceof JwtAuthenticationToken j) {
            id = j.getToken().getSubject();
            String pu = j.getToken().getClaimAsString("preferred_username");
            u = pu == null ? id : pu;
        }
        return new CurrentUser(id, u, a.getAuthorities().stream().map(Object::toString).collect(Collectors.toSet()), false);
    }
}

