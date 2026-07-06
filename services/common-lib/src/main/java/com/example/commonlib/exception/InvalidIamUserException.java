package com.example.commonlib.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 14:03
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Getter
public class InvalidIamUserException extends RuntimeException{
    private final HttpStatus status;

    public InvalidIamUserException(String message) {
        super(message);
        status = HttpStatus.UNAUTHORIZED;
    }
}
