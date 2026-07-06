package com.example.iamservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 13:36
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
public class TooManyRequestsException extends RuntimeException {
    private final HttpStatus status;

    public TooManyRequestsException(String message) {
        super(message);
        status = HttpStatus.TOO_MANY_REQUESTS;
    }
}
