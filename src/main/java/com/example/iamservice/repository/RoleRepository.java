package com.example.iamservice.repository;

import com.example.iamservice.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 14:32
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCodeAndDeletedFalse(String code);
    boolean existsByCodeAndDeletedFalse(String code);
    List<Role> findByCodeInAndDeletedFalse(Collection<String> codes);
}
