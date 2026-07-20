package com.example.iamservice.service.cache;

import com.example.iamservice.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 14:31
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class PermissionLookupService {

    private final PermissionRepository permissionLookupRepository;

    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<String> getPermissionCodes(Long userId) {
        return permissionLookupRepository.findActivePermissionCodesByUserId(userId);
    }

    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || permissionCode == null || permissionCode.isBlank()) {
            return false;
        }

        String normalizedPermission = normalize(permissionCode);

        return permissionLookupRepository.findActivePermissionCodesByUserId(userId).stream()
                .map(this::normalize)
                .anyMatch(normalizedPermission::equals);
    }

    @CacheEvict(value = "userPermissions", key = "#userId")
    public void evictUserPermissions(Long userId) {
        // Method intentionally empty. Annotation handles cache eviction.
    }

    @CacheEvict(value = "userPermissions", allEntries = true)
    public void evictAll() {
        // Method intentionally empty. Annotation handles cache eviction.
    }

    private String normalize(String permissionCode) {
        return permissionCode.trim().toUpperCase(Locale.ROOT);
    }
}
