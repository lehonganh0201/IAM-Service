package com.example.iamservice.repository;

import com.example.iamservice.domain.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    25/06/2026 at 16:43
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    @Query("""
        select rp.permissionId
        from RolePermission rp
        where rp.roleId = :roleId
        """)
    Set<Long> findPermissionIdsByRoleId(Long roleId);

    void deleteByRoleId(Long id);
}
