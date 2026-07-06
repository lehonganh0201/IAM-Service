package com.example.commonlib.exception;

import com.example.commonlib.api.RestData;
import com.example.commonlib.api.VsResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    09/06/2026 at 16:22
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<RestData<Object>> handleNotFoundException(NotFoundException ex) {
        return VsResponseUtil.error(
                ex.getMessage(),
                null,
                ex.getStatus()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<RestData<Object>> handleUnauthorizedException(UnauthorizedException ex) {
        return VsResponseUtil.error(
                ex.getMessage(),
                null,
                ex.getStatus()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<RestData<Object>> handleConflictException(ConflictException ex) {
        return VsResponseUtil.error(
                ex.getMessage(),
                null,
                ex.getStatus()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<RestData<Object>> handleBadRequestException(BadRequestException ex) {
        return VsResponseUtil.error(
                ex.getMessage(),
                null,
                ex.getStatus()
        );
    }

    @ExceptionHandler(UploadFileException.class)
    public ResponseEntity<RestData<Object>> handleUploadFileException(UploadFileException ex) {
        return VsResponseUtil.error(
                ex.getMessage(),
                null,
                ex.getStatus()
        );
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<RestData<Object>> handleTooManyRequestsException(TooManyRequestsException ex) {
        return VsResponseUtil.error(
                ex.getMessage(),
                null,
                ex.getStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestData<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại!";

        return VsResponseUtil.error(message, errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestData<Object>> handleException(Exception ex) {
        return VsResponseUtil.error(
                ex.getMessage(),
                null,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
