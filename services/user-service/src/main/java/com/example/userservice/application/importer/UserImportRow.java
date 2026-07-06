package com.example.userservice.application.importer;

import java.time.LocalDate;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:52
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record UserImportRow(int excelRowIndex, Integer stt, String username, String fullName, LocalDate dateOfBirth,
                            String street, String ward, String district, String province, Double yearsOfExperience) {
}
