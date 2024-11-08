package com.majestic.food.api.majestic_food_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.majestic.food.api.majestic_food_api.services.OrderService;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ExistsByOrderNameValidation implements ConstraintValidator<ExistsByOrderName, String> {

    @Autowired
    private OrderService service;
    
    @Override
    public boolean isValid(String orderName, ConstraintValidatorContext context) {
        if (service == null)
            return true;
        
        return !service.existsByOrderName(orderName);
    }
}
