package com.example.iamservice.service.audit;

import com.example.iamservice.domain.entity.common.SoftDeleteAuditing;
import com.example.iamservice.util.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 17:00
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class SoftDeleteService {

    private final CurrentUserProvider currentUserProvider;

    public void markDeleted(SoftDeleteAuditing entity, String reason) {
        Long currentUserId = currentUserProvider.getCurrentUserIdOrNull();

        entity.markDeleted(currentUserId, reason);
    }

    public void restore(SoftDeleteAuditing entity) {
        entity.restore();
    }
}
