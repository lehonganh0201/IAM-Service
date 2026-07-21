package com.example.commonlib.api;

import com.example.commonlib.constant.RestStatus;
import lombok.*;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 9:54
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestData<T> {
    private boolean success;
    private RestStatus status;
    private T data;
    private Object meta;
    private String message;
    private Object errors;

    public RestData(boolean success, RestStatus status, T data, Object meta, String message) {
        this.success = success;
        this.status = status;
        this.data = data;
        this.meta = meta;
        this.message = message;
    }

    public RestData(boolean success, RestStatus status, String message, Object errors) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.errors = errors;
    }
}
