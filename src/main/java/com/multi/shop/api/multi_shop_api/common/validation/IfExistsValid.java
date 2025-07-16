package com.multi.shop.api.multi_shop_api.common.validation;

import com.multi.shop.api.multi_shop_api.common.validation.validators.IfExistsValidatorValid;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = IfExistsValidatorValid.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IfExistsValid {
    String message() default "tiene el valor de un {field} existente";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    Class<?> entity();
    
    String field();
}