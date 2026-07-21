package com.example.commonlib.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:02
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        List<FieldErrorResponse> details
) {

    public static ApiError of(String code) {
        return new ApiError(code, null);
    }

    public static ApiError of(String code, List<FieldErrorResponse> details) {
        return new ApiError(code, details);
    }
}
