package com.example.iamservice.validation;

import com.example.iamservice.domain.dto.importer.ImportErrorItem;
import com.example.iamservice.domain.dto.importer.UserImportRow;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:37
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
