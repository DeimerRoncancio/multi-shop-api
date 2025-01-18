package com.multi.shop.api.multi_shop_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.multi.shop.api.multi_shop_api.services.CustomService;

@Component
public class IfExistsValidator implements ConstraintValidator<IfExists, Object> {
    private Class<?> entity;
    private String field;

    private final CustomService customService;

    public IfExistsValidator(CustomService service) {
        this.customService = service;
    }

    @Override
    public void initialize(IfExists annotation) {
        this.entity = annotation.entity();
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return (customService.ifExistsCustomField(entity.getSimpleName(), field, value) == 0);
    }
}
