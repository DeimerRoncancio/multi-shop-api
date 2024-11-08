package com.majestic.food.api.majestic_food_api.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = ExistsByOrderNameValidation.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ExistsByOrderName {

    String message() default "tiene el valor de un pedido existente";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
