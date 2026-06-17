package com.example.iamservice.config.seed;

import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 11:13
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Slf4j
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class SeedDataRunner implements CommandLineRunner {

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedLocalAdmin();
    }

    private void seedLocalAdmin() {
        String username = appProperties.getSeed().getAdminUsername();
        String email = appProperties.getSeed().getAdminEmail();
        String rawPassword = appProperties.getSeed().getAdminPassword();

        if (rawPassword == null || rawPassword.isBlank()) {
            log.warn("Skip local admin seed because admin password is blank");
            return;
        }

        Role userManagerRole = roleRepository.findByCodeAndDeletedFalse("USER_MANAGER")
                .orElseThrow(() -> new IllegalStateException("Missing role USER_MANAGER"));

        Role systemAdminRole = roleRepository.findByCodeAndDeletedFalse("SYSTEM_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Missing role SYSTEM_ADMIN"));

        User admin = userRepository.findByUsernameAndDeletedFalse(username)
                .or(() -> userRepository.findByEmailAndDeletedFalse(email))
                .orElseGet(User::new);

        boolean isNew = admin.getId() == null;

        String encodedPassword = passwordEncoder.encode(rawPassword);

        admin.setUsername(username);
        admin.setEmail(email);

        admin.setPasswordHash(encodedPassword);
        admin.setPasswordHash(encodedPassword);

        admin.setEnabled(true);
        admin.setLocked(false);
        admin.setDeleted(false);

        if (admin.getFirstName() == null) {
            admin.setFirstName("Local");
        }

        if (admin.getLastName() == null) {
            admin.setLastName("Admin");
        }

        admin.getRoles().clear();
        admin.getRoles().add(userManagerRole);
        admin.getRoles().add(systemAdminRole);

        userRepository.save(admin);

        if (isNew) {
            log.info("Seeded local admin user: username={}, email={}", username, email);
        } else {
            log.info("Local admin user already exists. Synchronized password, status and roles.");
        }
    }
}
