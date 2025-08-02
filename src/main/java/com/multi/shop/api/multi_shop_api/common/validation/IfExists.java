package com.multi.shop.api.multi_shop_api.common.validation;

import com.multi.shop.api.multi_shop_api.common.validation.validators.IfExistsValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = IfExistsValidation.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IfExists {
    String message() default "El valor del campo {field} ya esta en uso";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String field();

    String entity();
}
