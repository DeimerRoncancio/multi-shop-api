package com.multi.shop.api.multi_shop_api.catalog.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ExistingCategoriesValidation.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistingCategories {
    String message() default "Hay categorías invalidas";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
