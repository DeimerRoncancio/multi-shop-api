package com.multi.shop.api.multi_shop_api.common.validation.validators;

import com.multi.shop.api.multi_shop_api.common.services.CustomService;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public class IfExistsValidation implements ConstraintValidator<IfExists, String> {
    private final CustomService customService;
    private final HttpServletRequest request;
    private String entity;
    private String field;

    @Override
    public void initialize(IfExists constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.entity = constraintAnnotation.entity();
    }

    public IfExistsValidation(CustomService service, HttpServletRequest request) {
        this.customService = service;
        this.request = request;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;

        String id = getIdFromPath();

        if (id != null) {
            Long count = customService.ifExistsUpdate(value, id, entity, field);
            return count == 0;
        }

        Long count = customService.ifExists(value, entity, field);
        return count == 0;
    }

    private String getIdFromPath() {
        try {
            String uri = request.getRequestURI();
            String[] segments = uri.split("/");
            String id = segments[segments.length - 1];
            UUID.fromString(id);
            return id;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
