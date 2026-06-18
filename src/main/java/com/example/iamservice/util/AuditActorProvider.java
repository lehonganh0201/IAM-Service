package com.example.iamservice.util;

import com.example.iamservice.security.IamPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    18/06/2026 at 8:46
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class AuditActorProvider {
    public Actor currentActorOrAnonymous() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return Actor.anonymous();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof IamPrincipal iamPrincipal) {
            return new Actor(
                    iamPrincipal.userId(),
                    iamPrincipal.username(),
                    iamPrincipal.email(),
                    iamPrincipal.identityProvider()
            );
        }

        return Actor.anonymous();
    }

    public record Actor(
            Long userId,
            String username,
            String email,
            String identityProvider
    ) {

        public static Actor anonymous() {
            return new Actor(null, "anonymous", null, null);
        }
    }
}
