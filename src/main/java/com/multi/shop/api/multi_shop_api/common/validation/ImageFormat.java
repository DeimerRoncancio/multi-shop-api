package com.multi.shop.api.multi_shop_api.common.validation;

import com.multi.shop.api.multi_shop_api.common.validation.validators.ImageFormatValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ImageFormatValidation.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageFormat {
    String message() default "Tiene elementos que no son imágenes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int maxSize() default 2 * 1024 * 1024;
}
