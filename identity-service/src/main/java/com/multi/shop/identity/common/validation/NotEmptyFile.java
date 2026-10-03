package com.multi.shop.identity.common.validation;

import com.multi.shop.identity.common.validation.validators.NotEmptyFileValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEmptyFileValidation.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyFile {
    String message() default "Debe tener al menos una imagen";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
