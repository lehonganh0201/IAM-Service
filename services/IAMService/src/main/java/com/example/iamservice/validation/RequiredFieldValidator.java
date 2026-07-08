package com.example.iamservice.validation;

import com.example.iamservice.domain.dto.importer.ImportErrorItem;
import com.example.iamservice.domain.dto.importer.UserImportRow;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:40
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class RequiredFieldValidator implements ImportRowValidator {

    @Override
    public void validate(UserImportRow r, List<ImportErrorItem> e) {
        if (r.stt() == null) e.add(new ImportErrorItem(r.excelRowIndex(), "STT", null, "STT phải là số"));
        if (!StringUtils.hasText(r.username()))
            e.add(new ImportErrorItem(r.excelRowIndex(), "username", r.username(), "username không được trống"));
        if (!StringUtils.hasText(r.fullName()))
            e.add(new ImportErrorItem(r.excelRowIndex(), "Họ Tên", r.fullName(), "Họ Tên không được trống"));
        if (r.yearsOfExperience() == null)
            e.add(new ImportErrorItem(r.excelRowIndex(), "Số năm kinh nghiệm", null, "Số năm kinh nghiệm phải là number"));
        else if (r.yearsOfExperience() < 0)
            e.add(new ImportErrorItem(r.excelRowIndex(), "Số năm kinh nghiệm", r.yearsOfExperience(), "Số năm kinh nghiệm phải >= 0"));
    }
}

