package com.multi.shop.api.multi_shop_api.common.validation;

import com.multi.shop.api.multi_shop_api.common.validation.validators.NotEmptyFileValidation;
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
    String message() default "Debe tener almenos una imagen";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
