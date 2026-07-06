package com.example.commonlib.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 13:45
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
public class UnauthorizedException extends RuntimeException {
    private final HttpStatus status;

    public UnauthorizedException(String message) {
        super(message);
        status = HttpStatus.UNAUTHORIZED;
    }
}
