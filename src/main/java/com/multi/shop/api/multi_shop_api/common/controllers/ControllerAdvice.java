package com.multi.shop.api.multi_shop_api.common.controllers;

import com.multi.shop.api.multi_shop_api.common.exceptions.InvalidPasswordException;
import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.common.exceptions.PasswordMatchException;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ControllerAdvice.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> validationExceptionsHandler(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return errors;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Void> notFoundExceptionHandler(NotFoundException ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Void> invalidPasswordExceptionHandler(InvalidPasswordException ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(PasswordMatchException.class)
    public ResponseEntity<Void> passwordMatchExceptionHandler(PasswordMatchException ex) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
