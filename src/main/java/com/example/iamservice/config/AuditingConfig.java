package com.example.iamservice.config;

import com.example.iamservice.security.IamPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:55
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Configuration
@EnableJpaAuditing
public class AuditingConfig {
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new AuditorAwareImpl();
    }

    private static class AuditorAwareImpl implements AuditorAware<Long> {
        @Override
        public Optional<Long> getCurrentAuditor() {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof IamPrincipal iamPrincipal) {
                return Optional.ofNullable(iamPrincipal.userId());
            }

            return Optional.empty();
        }
    }
}
