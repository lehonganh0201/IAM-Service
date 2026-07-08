package com.example.iamservice.domain.dto.request;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 12:01
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record UserSearchQuery(String keyword, String province, Double minYears, Double maxYears) {
}
