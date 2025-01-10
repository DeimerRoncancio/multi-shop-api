package com.multi.shop.api.multi_shop_api.validation;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class SizeConstraintValidator implements ConstraintValidator<SizeConstraint, String>{

    private int min;
    private int max;
    
    @Override
    public void initialize(SizeConstraint anotation) {
        this.min = anotation.min();
        this.max = anotation.max();
    }
    
    @Override
    public boolean isValid(String target, ConstraintValidatorContext context) {
        return !(target == null || target.isBlank()) ? 
            (target.length() >= min && target.length() <= max) : true;
    }
}
