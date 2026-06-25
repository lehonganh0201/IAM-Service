package com.example.iamservice.repository;

import com.example.iamservice.domain.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Optional<Permission> findByCodeAndDeletedFalse(String code);
    boolean existsByCodeAndDeletedFalse(String code);

    List<Permission> findByCodeInAndDeletedFalse(Collection<String> codes);

    Optional<Permission> findByIdAndDeletedFalse(Long id);

    @Query(value = """
        select distinct p.code
        from users u
        join user_roles ur on ur.user_id = u.id
        join roles r on r.id = ur.role_id
        join role_permissions rp on rp.role_id = r.id
        join permissions p on p.id = rp.permission_id
        where u.id = :userId
          and u.deleted = false
          and u.enabled = true
          and u.locked = false
          and r.deleted = false
          and p.deleted = false
        """, nativeQuery = true)
    Set<String> findActivePermissionCodesByUserId(@Param("userId") Long userId);
}
