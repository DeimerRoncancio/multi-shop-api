package com.majestic.food.api.majestic_food_api.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = ExistsByPhoneNumberValidation.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ExistsByPhoneNumber {

    String message() default "tiene un valor de un numero ya existente";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
