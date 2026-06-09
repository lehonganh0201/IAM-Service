package com.example.iamservice.security.jwt;

import com.example.iamservice.exception.UnauthorizedException;
import com.example.iamservice.security.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 13:28
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@Log4j2
public class JwtTokenProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
//    private static final String USERNAME_KEY = "username";
    private static final String AUTHORITIES_KEY = "auth";

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private Integer expirationMilliseconds;

    @Value("${app.jwt.refresh-expiration-ms}")
    private Integer refreshExpirationMilliseconds;

    public String generateToken(UserPrincipal userPrincipal, boolean isRefreshToken) {
        Map<String, Object> claims = buildClaims(userPrincipal, isRefreshToken);

        Integer expirationTime = isRefreshToken ? refreshExpirationMilliseconds : expirationMilliseconds;

        return buildJwtToken(claims, userPrincipal.getUsername(), expirationTime);
    }

    public String extractUsername(String token) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();

        checkUsernameEmpty(username);
        return username;
    }

    public boolean isRefreshToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get(CLAIM_TYPE, String.class).equals(TYPE_REFRESH);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new UnauthorizedException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new UnauthorizedException("JWT claims string is empty");
        }
    }

    private Claims parseClaims(String token) {
        if (ObjectUtils.isEmpty(token)) {
            throw new UnauthorizedException("Token is missing");
        }
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void checkUsernameEmpty(String username) {
        if (username == null || username.isEmpty()) {
            log.warn("Username claim is missing in the token");
            throw new UnauthorizedException("Invalid token: username claim is missing");
        }
    }

    private String buildJwtToken(Map<String, Object> claims, String username, Integer expirationTime) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Object> buildClaims(UserPrincipal userPrincipal, boolean isRefreshToken) {
        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, isRefreshToken ? TYPE_REFRESH : TYPE_ACCESS);
//        claims.put(USERNAME_KEY, userPrincipal.getUsername());
        claims.put(AUTHORITIES_KEY, authorities);
        return claims;
    }
}
