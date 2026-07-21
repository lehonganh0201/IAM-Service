package com.example.commonlib.factory;

import com.example.commonlib.api.RestData;
import com.example.commonlib.constant.RestStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 9:55
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public class VsResponseUtil {

    public static <T> ResponseEntity<RestData<T>> success(T data, Object meta, String message, HttpStatus status) {
        RestData<T> response = RestData.<T>builder()
                .success(true)
                .status(RestStatus.SUCCESS)
                .data(data)
                .meta(meta)
                .message(message)
                .build();
        return new ResponseEntity<>(response, status);
    }

    public static <T> ResponseEntity<RestData<T>> success(T data, String message, HttpStatus status) {
        return success(data, null, message, status);
    }

    public static <T> ResponseEntity<RestData<T>> error(String message, Object errors, HttpStatus status) {
        RestData<T> response = RestData.<T>builder()
                .success(false)
                .status(RestStatus.ERROR)
                .message(message)
                .errors(errors)
                .build();
        return new ResponseEntity<>(response, status);
    }

    public static <T> ResponseEntity<RestData<T>> responseWithHeaders(T data, Object meta, String message,
                                                                      HttpStatus status, HttpHeaders headers) {
        RestData<T> response = RestData.<T>builder()
                .success(true)
                .status(RestStatus.SUCCESS)
                .data(data)
                .meta(meta)
                .message(message)
                .build();
        return new ResponseEntity<>(response, headers, status);
    }
}
