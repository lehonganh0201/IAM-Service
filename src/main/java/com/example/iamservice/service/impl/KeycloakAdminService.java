package com.example.iamservice.service.impl;

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

    public KeycloakUserProvisioningResult createUser(KeycloakRegisterRequest request) {
        UserRepresentation user = getUserRepresentation(request);

        try (Response response = realmUsers().create(user)) {
            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                String errorBody = response.readEntity(String.class);

                throw new BadRequestException(
                        "Failed to create Keycloak user. status="
                                + response.getStatus()
                                + ", body="
                                + errorBody
                );
            }

            String keycloakUserId = extractCreatedUserId(response.getLocation());

            return new KeycloakUserProvisioningResult(
                    keycloakUserId,
                    request.username(),
                    request.email()
            );
        }
    }

    private static UserRepresentation getUserRepresentation(KeycloakRegisterRequest request) {
        UserRepresentation user = new UserRepresentation();

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));
        return user;
    }

    public void disableUser(String keycloakUserId) {
        updateEnabled(keycloakUserId, false);
    }

    public void enableUser(String keycloakUserId) {
        updateEnabled(keycloakUserId, true);
    }

    public void resetPassword(String keycloakUserId, String newPassword, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(temporary);

        userResource(keycloakUserId).resetPassword(credential);
    }

    public void deleteUser(String keycloakUserId) {
        userResource(keycloakUserId).remove();
    }

    public void updateUserProfile(
            String keycloakUserId,
            String firstName,
            String lastName,
            Boolean enabled
    ) {
        UserResource userResource = userResource(keycloakUserId);
        UserRepresentation user = userResource.toRepresentation();

        user.setFirstName(firstName);
        user.setLastName(lastName);

        if (enabled != null) {
            user.setEnabled(enabled);
        }

        userResource.update(user);
    }


    private void updateEnabled(String keycloakUserId, boolean enabled) {
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

    private String extractCreatedUserId(URI location) {
        if (location == null) {
            throw new BadRequestException("Keycloak response does not contain Location header");
        }

        String path = location.getPath();
        int lastSlashIndex = path.lastIndexOf('/');

        if (lastSlashIndex < 0 || lastSlashIndex == path.length() - 1) {
            throw new BadRequestException("Cannot extract Keycloak user id from Location: " + location);
        }

        return path.substring(lastSlashIndex + 1);
    }
}
