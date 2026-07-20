package com.example.userservice.application.dto.response;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:48
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record ImportErrorItem(int rowIndex, String field, Object value, String message) {
}
