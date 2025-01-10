package com.multi.shop.api.multi_shop_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

import com.multi.shop.api.multi_shop_api.services.CustomService;

@Component
public class IfExistsValidator implements ConstraintValidator<IfExists, Object> {

    @PersistenceContext
    private EntityManager entityManager;
    private Class<?> entity;
    private String field;

    private final CustomService service;

    public IfExistsValidator(CustomService service) {
        this.service = service;
    }

    @Override
    public void initialize(IfExists annotation) {
        this.entity = annotation.entity();
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (entityManager == null)
           return false;

        return (service.ifExistsCustomField(entity.getSimpleName(), field, value) == 0);
    }
}
