package com.example.iamservice.service.impl;

import com.example.iamservice.domain.dto.request.AuthRequest;
import com.example.iamservice.domain.dto.request.UserRequest;
import com.example.iamservice.domain.dto.response.AuthResponse;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.exception.UnauthorizedException;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.security.UserPrincipal;
import com.example.iamservice.security.jwt.JwtTokenProvider;
import com.example.iamservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:40
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    private static final boolean IS_REFRESH_TOKEN = true;

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            if (Objects.isNull(authentication)) {
                throw new UnauthorizedException("Login failed with wrong email or password");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("User logged in successfully: {}", request.getEmail());
            return generateAuthResponse((UserPrincipal) authentication.getPrincipal());

        } catch (Exception ex) {
            log.warn("Login failed for username or email {}: {}", request.getEmail(), ex.getMessage());
            throw new UnauthorizedException("Login failed");
        }
    }

    @Override
    public AuthResponse refreshToken(String token) {
        String cleanedToken = cleanToken(token);

        if (jwtTokenProvider.validateToken(cleanedToken) && jwtTokenProvider.isRefreshToken(cleanedToken)) {
            String username = jwtTokenProvider.extractUsername(cleanedToken);
            User user = findUserWithEmail(username);

            log.info("Token refreshed for user: {}", username);

            return generateAuthResponse(UserPrincipal.create(user));
        }
        throw new UnauthorizedException("Refresh token cannot access");
    }

    private String cleanToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException("Token is missing");
        }
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private User findUserWithEmail(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private AuthResponse generateAuthResponse(UserPrincipal user) {

        String accessToken = jwtTokenProvider.generateToken(user, !IS_REFRESH_TOKEN);
        String refreshToken = jwtTokenProvider.generateToken(user, IS_REFRESH_TOKEN);

        String roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return AuthResponse.builder()
                .email(user.getEmail())
                .accessToken(accessToken)
                .role(roles)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}
