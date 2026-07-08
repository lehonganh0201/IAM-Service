package com.example.iamservice.repository;

import com.example.iamservice.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    25/06/2026 at 16:41
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    void deleteByUserId(Long id);

    @Query("""
        select ur.roleId
        from UserRole ur
        where ur.userId = :userId
    """)
    Set<Long> findRoleIdsByUserId(Long userId);
}
