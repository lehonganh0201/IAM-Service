package com.example.userservice.application.importer;

import com.example.userservice.application.dto.response.ImportErrorItem;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:59
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class BirthDateValidator implements ImportRowValidator {

    @Override
    public void validate(UserImportRow r, List<ImportErrorItem> e) {
        if (r.dateOfBirth() != null && r.dateOfBirth().isAfter(LocalDate.now()))
            e.add(new ImportErrorItem(r.excelRowIndex(), "Ngày sinh", r.dateOfBirth(), "Ngày sinh không được ở tương lai"));
    }
}

