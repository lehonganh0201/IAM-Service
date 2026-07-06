package com.example.iamservice.config.seed;

import com.example.iamservice.config.properties.AppProperties;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.domain.entity.UserRole;
import com.example.iamservice.exception.NotFoundException;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    private final UserRoleRepository userRoleRepository;
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
                .orElseThrow(() -> new NotFoundException("Missing role USER_MANAGER"));

        Role systemAdminRole = roleRepository.findByCodeAndDeletedFalse("SYSTEM_ADMIN")
                .orElseThrow(() -> new NotFoundException("Missing role SYSTEM_ADMIN"));

        User admin = userRepository.findByUsernameAndDeletedFalse(username)
                .or(() -> userRepository.findByEmailAndDeletedFalse(email))
                .orElseGet(User::new);

        boolean isNew = admin.getId() == null;

        String encodedPassword = passwordEncoder.encode(rawPassword);

        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(encodedPassword);

        admin.setEnabled(true);
        admin.setLocked(false);
        admin.setDeleted(false);

        if (admin.getFirstName() == null) admin.setFirstName("Local");
        if (admin.getLastName() == null) admin.setLastName("Admin");

        admin = userRepository.save(admin);

        userRoleRepository.deleteByUserId(admin.getId());

        assignRole(admin.getId(), userManagerRole.getId());
        assignRole(admin.getId(), systemAdminRole.getId());

        log.info(isNew
                        ? "Seeded local admin user: username={}, email={}"
                        : "Updated local admin user: roles + credentials synced",
                username, email);
    }

    private void assignRole(Long userId, Long roleId) {
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        ur.setCreatedAt(LocalDateTime.now());
        ur.setUpdatedAt(LocalDateTime.now());

        userRoleRepository.save(ur);
    }
}