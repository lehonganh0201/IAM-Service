package com.example.iamservice.security.jwt;

import com.example.commonlib.exception.BadRequestException;
import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.util.RSAKeyUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "typ";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private final RSAKeyUtil rsaKeyUtil;

    private final AppProperties appProperties;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        String secret = appProperties.getJwt().getSecret();

        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new BadRequestException("JWT secret must be at least 32 bytes");
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(appProperties.getJwt().getExpirationMs());

        PrivateKey privateKey;
        try {
            privateKey = rsaKeyUtil.getPrivateKey();
        } catch (Exception e) {
            throw new BadRequestException("Failed to load RSA private key: " + e.getMessage());
        }

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        return ACCESS_TOKEN_TYPE.equals(tokenType);
    }

    public Long getUserId(String token) {
        String subject = parseClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    public String getUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    private Claims parseClaims(String token) {
        PublicKey publicKey;
        try {
            publicKey = rsaKeyUtil.getPublicKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}