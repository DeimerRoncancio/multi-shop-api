package com.multi.shop.api.multi_shop_api.products.validation;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;

@Component
public class IfExistsCategoriesValidation implements Validator {
    private final ProductCategoryService categoryService;

    public IfExistsCategoriesValidation(ProductCategoryService categoryRepository) {
        this.categoryService = categoryRepository;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        List<String> categoriesList = ((List<?>) target).stream()
            .map(item -> (String) item)
            .toList();

        List<ProductCategory> categories = categoryService.findCategoriesByName(categoriesList);

        List<String> categoriesDoesntExists = categoriesList.stream()
            .filter(category -> categories.stream()
            .noneMatch(cats -> cats.getCategoryName().equals(category)))
            .toList();
        
        if (!categoriesDoesntExists.isEmpty()){
            categoriesDoesntExists.forEach(item -> {
                String messageError = "tiene un error. La categoría " + item + " no existe";
                errors.rejectValue("categories", "", messageError);
            });
        }
    }
}
