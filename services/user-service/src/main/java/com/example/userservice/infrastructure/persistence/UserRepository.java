package com.example.userservice.infrastructure.persistence;

import com.example.userservice.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:23
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    boolean existsByUsernameAndStatusNot(String username, UserStatus status);

    Optional<UserEntity> findByIdAndStatusNot(UUID id, UserStatus status);

    List<UserEntity> findByUsernameInAndStatusNot(Collection<String> usernames, UserStatus status);
}
