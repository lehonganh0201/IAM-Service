package com.example.commonlib.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:02
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        ApiError error,
        Instant timestamp,
        String path,
        String requestId
) {

    public static <T> ApiResponse<T> success(
            String message,
            T data,
            String path,
            String requestId
    ) {
        return new ApiResponse<>(
                true,
                message,
                data,
                null,
                Instant.now(),
                path,
                requestId
        );
    }

    public static ApiResponse<Void> success(
            String message,
            String path,
            String requestId
    ) {
        return new ApiResponse<>(
                true,
                message,
                null,
                null,
                Instant.now(),
                path,
                requestId
        );
    }

    public static ApiResponse<Void> error(
            String message,
            ApiError error,
            String path,
            String requestId
    ) {
        return new ApiResponse<>(
                false,
                message,
                null,
                error,
                Instant.now(),
                path,
                requestId
        );
    }
}
