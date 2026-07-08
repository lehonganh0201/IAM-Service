package com.example.iamservice.security.keycloak;

import com.example.commonlib.exception.InvalidIamUserException;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.security.IamPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 14:04
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new InvalidIamUserException("Keycloak token does not contain subject");
        }

        User user = userRepository.findByKeycloakUserIdAndDeletedFalse(keycloakUserId)
                .orElseThrow(() -> new InvalidIamUserException(
                        "Keycloak user is not synchronized in IAM database"
                ));

        validateUser(user);

        IamPrincipal principal = new IamPrincipal(
                user.getId(),
                resolveUsername(user, jwt),
                resolveEmail(user, jwt),
                "KEYCLOAK"
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                jwt,
                Collections.emptyList()
        );
    }

    private void validateUser(User user) {
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new InvalidIamUserException("User is disabled");
        }

        if (Boolean.TRUE.equals(user.getLocked())) {
            throw new InvalidIamUserException("User is locked");
        }

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new InvalidIamUserException("User is deleted");
        }
    }

    private String resolveUsername(User user, Jwt jwt) {
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }

        String preferredUsername = jwt.getClaimAsString("preferred_username");

        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        return user.getEmail();
    }

    private String resolveEmail(User user, Jwt jwt) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }

        return jwt.getClaimAsString("email");
    }
}
