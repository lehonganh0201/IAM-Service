package com.example.iamservice.repository;

import com.example.iamservice.domain.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 10:10
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCodeAndDeletedFalse(String code);
    boolean existsByCodeAndDeletedFalse(String code);

    List<Permission> findByCodeInAndDeletedFalse(Collection<String> codes);

    Optional<Permission> findByIdAndDeletedFalse(Long id);

    Page<Permission> findByDeletedFalse(Pageable pageable);

    Page<Permission> findByDeletedFalseAndCodeContainingIgnoreCaseOrDeletedFalseAndNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

    @Query("""
            select distinct p.code
            from User u
            join u.roles r
            join r.permissions p
            where u.id = :userId
              and u.deleted = false
              and u.enabled = true
              and u.locked = false
              and r.deleted = false
              and p.deleted = false
            """)
    Set<String> findActivePermissionCodesByUserId(Long userId);
}
