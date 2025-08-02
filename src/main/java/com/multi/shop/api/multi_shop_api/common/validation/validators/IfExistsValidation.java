package com.multi.shop.api.multi_shop_api.common.validation.validators;

import com.multi.shop.api.multi_shop_api.common.services.CustomService;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public class IfExistsValidation implements ConstraintValidator<IfExists, Object> {
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
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        String value = "";

        if (target instanceof Long number) value = number.toString();
        else value = target.toString();

        if (value == null || value.isBlank()) return true;

        String id = getIdFromPath();

        if (id != null) {
            if (customService.ifExistsById(id, entity)) return true;
            return customService.ifExistsUpdate(value, id, entity, field);
        }

        return customService.ifExists(value, entity, field);
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
