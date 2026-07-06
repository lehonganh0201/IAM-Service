package com.example.iamservice.repository;

import com.example.iamservice.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 13:25
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByKeycloakUserIdAndDeletedFalse(String keycloakUserId);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByUsernameAndDeletedFalse(String username);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesByIdAndDeletedFalse(Long id);
}
