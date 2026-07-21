package com.example.storageservice.domain.policy;

import com.example.commonlib.security.CurrentUser;
import com.example.storageservice.domain.model.FileVisibility;
import com.example.storageservice.infrastructure.persistence.FileMetadataEntity;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:33
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class StorageFilePermissionPolicy {
    public boolean canUpload(FileVisibility s, CurrentUser u) {
        if (u.isAnonymous()) return false;
        return s == FileVisibility.PUBLIC || u.isAdmin() || u.hasAuthority("storage:private:write") || u.hasAuthority("storage:file:write");
    }

    public boolean canRead(FileMetadataEntity f, CurrentUser u) {
        if (f.getVisibility() == FileVisibility.PUBLIC) return !u.isAnonymous();
        return owner(f, u) || u.isAdmin() || u.hasAuthority("storage:private:read") || u.hasAuthority("storage:file:read");
    }

    public boolean canUpdate(FileMetadataEntity f, CurrentUser u) {
        return !u.isAnonymous() && (owner(f, u) || u.isAdmin() || u.hasAuthority("storage:file:update") || u.hasAuthority("storage:private:write"));
    }

    public boolean canDelete(FileMetadataEntity f, CurrentUser u) {
        return !u.isAnonymous() && (owner(f, u) || u.isAdmin() || u.hasAuthority("storage:file:delete") || u.hasAuthority("storage:private:delete"));
    }

    public boolean canDownload(FileMetadataEntity f, CurrentUser u) {
        return canRead(f, u);
    }

    private boolean owner(FileMetadataEntity f, CurrentUser u) {
        return !u.isAnonymous() && f.getOwnerId() != null && f.getOwnerId().equals(u.id());
    }
}

