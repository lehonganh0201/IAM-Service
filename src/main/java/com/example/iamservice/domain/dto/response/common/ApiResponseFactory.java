package com.example.iamservice.domain.dto.response.common;

import com.example.iamservice.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:07
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class ApiResponseFactory {

    private final RequestContext requestContext;

    public <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(
                message,
                data,
                requestContext.path(),
                requestContext.requestId()
        );
    }

    public ApiResponse<Void> success(String message) {
        return ApiResponse.success(
                message,
                requestContext.path(),
                requestContext.requestId()
        );
    }

    public ApiResponse<Void> error(String message, ApiError error) {
        return ApiResponse.error(
                message,
                error,
                requestContext.path(),
                requestContext.requestId()
        );
    }
}
