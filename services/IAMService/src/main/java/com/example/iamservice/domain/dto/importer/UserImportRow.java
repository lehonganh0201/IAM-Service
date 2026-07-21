package com.example.iamservice.domain.dto.importer;

import java.time.LocalDate;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:32
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record UserImportRow(int excelRowIndex, Integer stt, String username, String fullName, LocalDate dateOfBirth,
                            String street, String ward, String district, String province, Double yearsOfExperience) {
}
