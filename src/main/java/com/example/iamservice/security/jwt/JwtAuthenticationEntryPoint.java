package com.example.iamservice.security.jwt;

import com.example.iamservice.base.RestData;
import com.example.iamservice.base.VsResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:02
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = determineErrorMessage(authException);

        ResponseEntity<RestData<String>> restData = VsResponseUtil.error(
                message,
                "Request requires authentication",
                HttpStatus.UNAUTHORIZED
        );

        writeResponse(response, restData);
    }

    private String determineErrorMessage(AuthenticationException authException) {
        String exceptionName = authException.getClass().getSimpleName();

        return switch (exceptionName) {
            case "BadCredentialsException" -> "Invalid username or password";
            case "UsernameNotFoundException" -> "User not found";
            case "ExpiredJwtException", "InvalidTokenException" -> "Token is invalid or expired";
            default -> "Authentication failed: " + authException.getMessage();
        };
    }

    private void writeResponse(HttpServletResponse response, ResponseEntity<RestData<String>> restData) throws IOException {
        try {
            response.getOutputStream().write(objectMapper.writeValueAsBytes(restData));
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Failed to write authentication error response", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getOutputStream().write(objectMapper.writeValueAsBytes(
                    VsResponseUtil.error("Internal server error", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR)
            ));
        }
    }
}
