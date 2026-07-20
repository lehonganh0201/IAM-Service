package com.example.userservice.application.exporter;

import com.example.userservice.infrastructure.persistence.UserEntity;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 17:06
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public interface UserExportStrategy {
    String format();

    String contentType();

    String filename();

    byte[] export(List<UserEntity> users);
}
