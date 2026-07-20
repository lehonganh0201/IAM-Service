package com.example.userservice.application.importer;

import com.example.userservice.application.dto.response.ImportErrorItem;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:54
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public interface ImportRowValidator {
    void validate(UserImportRow row, List<ImportErrorItem> errors);
}
