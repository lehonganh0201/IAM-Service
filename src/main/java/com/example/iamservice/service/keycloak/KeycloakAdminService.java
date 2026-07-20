package com.example.iamservice.service.keycloak;

import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.domain.dto.request.KeycloakRegisterRequest;
import com.example.iamservice.domain.dto.response.KeycloakUserProvisioningResult;
import com.example.iamservice.exception.BadRequestException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 13:23
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    private final Keycloak keycloakAdminClient;
    private final AppProperties appProperties;

    private static final String VERIFY_EMAIL_ACTION = "VERIFY_EMAIL";
    private static final String PASSWORD_CREDENTIAL_TYPE = CredentialRepresentation.PASSWORD;

    public KeycloakUserProvisioningResult createUser(KeycloakRegisterRequest request) {
        UserRepresentation user = buildUserRepresentation(request);

        try (Response response = realmUsers().create(user)) {
            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                String errorBody = response.readEntity(String.class);
                throw new BadRequestException(
                        "Failed to create Keycloak user. Status: %d, Body: %s"
                                .formatted(response.getStatus(), errorBody)
                );
            }

            String userId = extractUserIdFromLocation(response.getLocation());

            sendVerificationEmailAsync(userId);

            return new KeycloakUserProvisioningResult(userId, request.username(), request.email());
        }
    }

    public void disableUser(String keycloakUserId) {
        updateUserEnabled(keycloakUserId, false);
    }

    public void enableUser(String keycloakUserId) {
        updateUserEnabled(keycloakUserId, true);
    }

    public void resetPassword(String keycloakUserId, String newPassword, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(PASSWORD_CREDENTIAL_TYPE);
        credential.setValue(newPassword);
        credential.setTemporary(temporary);

        userResource(keycloakUserId).resetPassword(credential);
    }

    public void deleteUser(String keycloakUserId) {
        userResource(keycloakUserId).remove();
    }

    public void updateUserProfile(String keycloakUserId, String firstName, String lastName, Boolean enabled) {
        UserResource userResource = userResource(keycloakUserId);
        UserRepresentation user = userResource.toRepresentation();

        user.setFirstName(firstName);
        user.setLastName(lastName);

        if (enabled != null) {
            user.setEnabled(enabled);
        }

        userResource.update(user);
    }

    @Async
    public void sendVerificationEmailAsync(String keycloakUserId) {
        try {
            userResource(keycloakUserId).sendVerifyEmail();
        } catch (Exception e) {
            throw new BadRequestException("Cannot send verify email");
        }
    }

    private UserRepresentation buildUserRepresentation(KeycloakRegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setRequiredActions(List.of(VERIFY_EMAIL_ACTION));

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(PASSWORD_CREDENTIAL_TYPE);
        credential.setValue(request.password());
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));
        return user;
    }

    private void updateUserEnabled(String keycloakUserId, boolean enabled) {
        UserResource userResource = userResource(keycloakUserId);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(enabled);
        userResource.update(user);
    }

    private UserResource userResource(String keycloakUserId) {
        return realmUsers().get(keycloakUserId);
    }

    private UsersResource realmUsers() {
        return keycloakAdminClient
                .realm(appProperties.getKeycloak().getRealm())
                .users();
    }

    private String extractUserIdFromLocation(URI location) {
        if (location == null) {
            throw new BadRequestException("Keycloak response does not contain Location header");
        }

        String path = location.getPath();
        int lastSlash = path.lastIndexOf('/');

        if (lastSlash <= 0 || lastSlash == path.length() - 1) {
            throw new BadRequestException("Cannot extract user ID from Location: " + location);
        }

        return path.substring(lastSlash + 1);
    }
}