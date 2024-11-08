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
    private String querySql = "SELECT COUNT(e) FROM %s e WHERE e.%s = :value";

    @Override
    public void initialize(IfExists constraintAnnotation) {
        this.entity = constraintAnnotation.entity();
        this.field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String query = String.format(querySql, entity.getSimpleName(), field);

        Long ifExists = entityManager.createQuery(query, Long.class)
                                     .setParameter("value", value)
                                     .getSingleResult();
        
        return (ifExists == 0);
    }
}
