package com.majestic.food.api.majestic_food_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

@Component
public class IfExistsValidator implements ConstraintValidator<IfExists, Object> {

    @PersistenceContext
    private EntityManager entityManager;

    private Class<?> entity;
    private String field;
    private String JPQL = "SELECT COUNT(e) FROM %s e WHERE e.%s = :value";

    @Override
    public void initialize(IfExists annotation) {
        this.entity = annotation.entity();
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (entityManager == null)
           return false;

        String EXISTS_JPQL = String.format(JPQL, entity.getSimpleName(), field);

        Long ifExists = entityManager.createQuery(EXISTS_JPQL, Long.class)
                                    .setParameter("value", value)
                                    .getSingleResult();

        return (ifExists == 0);
    }
}
