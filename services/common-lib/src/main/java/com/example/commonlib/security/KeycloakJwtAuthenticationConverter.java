package com.example.commonlib.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:08
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String u = jwt.getClaimAsString("preferred_username");
        return new JwtAuthenticationToken(jwt, authorities(jwt), u == null ? jwt.getSubject() : u);
    }

    private Collection<GrantedAuthority> authorities(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        Map<String, Object> ra = jwt.getClaim("realm_access");
        if (ra != null && ra.get("roles") instanceof Collection<?> rr) rr.forEach(r -> add(roles, String.valueOf(r)));
        Map<String, Object> res = jwt.getClaim("resource_access");
        if (res != null) res.values().forEach(v -> {
            if (v instanceof Map<?, ?> m && m.get("roles") instanceof Collection<?> cr)
                cr.forEach(r -> add(roles, String.valueOf(r)));
        });
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    private void add(Set<String> roles, String r) {
        roles.add(r);
        roles.add("ROLE_" + r);
    }
}
