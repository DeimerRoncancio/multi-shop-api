package com.multi.shop.api.multi_shop_api.validation;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.multi.shop.api.multi_shop_api.entities.Product;
import com.multi.shop.api.multi_shop_api.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.repositories.ProductCategoryRepository;

@Component
public class IfExistsCategories implements Validator {

    private final ProductCategoryRepository categoryRepository;

    public IfExistsCategories(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        List<String> categoriesList = ((List<?>) target).stream()
            .filter(item -> item instanceof String)
            .map(item -> (String) item)
            .toList();

        List<ProductCategory> categories = categoryRepository.findByCategoryNameIn(categoriesList);

        List<String> categoriesDoesntExists = categoriesList.stream()
            .filter(category -> categories.stream()
            .noneMatch(cats -> cats.getCategoryName()
            .equals(category))).toList();
        
        if (!categoriesDoesntExists.isEmpty()){
            categoriesDoesntExists.forEach(item -> {
                errors.rejectValue("categories", "", "tiene un error. La categoría " + item + " no existe");
            });
        }
    }
}
