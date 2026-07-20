package com.example.iamservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 9:10
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
public class BadRequestException extends RuntimeException {
    private final HttpStatus status;

    public BadRequestException(String message) {
        super(message);
        status = HttpStatus.BAD_REQUEST;
    }
}
