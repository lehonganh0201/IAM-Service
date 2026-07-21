package com.example.iamservice.validation;


import com.example.iamservice.domain.dto.importer.ImportErrorItem;
import com.example.iamservice.domain.dto.importer.UserImportRow;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:26
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface ImportRowValidator {
    void validate(UserImportRow row, List<ImportErrorItem> errors);
}
