package com.example.iamservice.config;

import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.repository.RoleRepository;
import com.example.iamservice.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 15:39
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Configuration
@Log4j2
public class InitConfig {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("USER").build()
                    ));

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("ADMIN").build()
                    ));

            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = User.builder()
                        .firstName("Admin")
                        .lastName("User")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .phoneNumber("0123456789")
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .avatarUrl(null)
                        .roleId(adminRole.getId())
                        .emailVerified(true)
                        .build();
                userRepository.save(admin);
            }

            if (!userRepository.existsByEmail("user@example.com")) {
                User user = User.builder()
                        .firstName("Normal")
                        .lastName("User")
                        .email("user@example.com")
                        .password(passwordEncoder.encode("user123"))
                        .phoneNumber("0987654321")
                        .dateOfBirth(LocalDate.of(1995, 5, 5))
                        .avatarUrl(null)
                        .roleId(userRole.getId())
                        .emailVerified(true)
                        .build();
                userRepository.save(user);
            }

            log.info("Init data completed!");
        };
    }
}
