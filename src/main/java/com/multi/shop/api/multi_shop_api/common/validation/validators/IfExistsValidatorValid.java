package com.multi.shop.api.multi_shop_api.common.validation.validators;

import com.multi.shop.api.multi_shop_api.common.validation.IfExistsValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.multi.shop.api.multi_shop_api.common.services.CustomService;

@Component
public class IfExistsValidatorValid implements ConstraintValidator<IfExistsValid, Object> {
    private Class<?> entity;
    private String field;

    private final CustomService customService;

    public IfExistsValidatorValid(CustomService service) {
        this.customService = service;
    }

    @Override
    public void initialize(IfExistsValid annotation) {
        this.entity = annotation.entity();
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return (customService.ifExists(value, entity.getSimpleName(), field) == 0);
    }
}
