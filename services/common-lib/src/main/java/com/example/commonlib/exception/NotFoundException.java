package com.example.commonlib.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 15:04
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
public class NotFoundException extends RuntimeException {
    private final HttpStatus status;

    public NotFoundException(String message) {
        super(message);
        status = HttpStatus.NOT_FOUND;
    }
}
