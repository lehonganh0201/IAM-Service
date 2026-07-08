package com.example.iamservice.service.exporter;

import com.example.iamservice.domain.entity.User;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:59
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface UserExportStrategy {
    String format();

    String contentType();

    String filename();

    byte[] export(List<User> users);
}
