package com.example.commonlib.security;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:07
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record CurrentUser(String id, String username, Set<String> authorities, boolean isAnonymous) {
    public boolean hasAuthority(String a) {
        return authorities != null && authorities.contains(a);
    }

    public boolean isAdmin() {
        return hasAuthority("storage:file:admin") || hasAuthority("iam:user:manage") || hasAuthority("ROLE_admin") || hasAuthority("admin");
    }

    public static CurrentUser anonymous() {
        return new CurrentUser("anonymous", "anonymous", Set.of(), true);
    }
}

