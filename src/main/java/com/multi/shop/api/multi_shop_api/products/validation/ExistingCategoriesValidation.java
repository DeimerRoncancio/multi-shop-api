package com.multi.shop.api.multi_shop_api.products.validation;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductCategoryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ExistingCategoriesValidation implements ConstraintValidator<ExistingCategories, List<String>> {
    private final ProductCategoryRepository repository;

    public ExistingCategoriesValidation(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isValid(List<String> categories, ConstraintValidatorContext context) {
        List<ProductCategory> currentCategories = repository.findByCategoryNameIn(categories);

        List<String> invalidCategories = categories.stream()
            .filter(cat -> currentCategories.stream()
            .noneMatch(c -> c.getCategoryName().equals(cat)))
            .toList();

        if (!invalidCategories.isEmpty()){
            String message = messageBuilder(invalidCategories);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

            return false;
        }

        return true;
    }

    public String messageBuilder(List<String> categories) {
        if (categories.size() > 1) {
            return "Las categorías "
                + String.join(", ",categories.subList(0, categories.size() - 1))
                + " Y " + categories.get(categories.size() - 1)
                + " no existen";
        } else {
            return "La categoría " + categories.get(0) + " no existe";
        }
    }
}
