package com.example.userservice.application.dto.request;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 15:32
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record UserSearchQuery(String keyword, String province, Double minYears, Double maxYears) {
}
