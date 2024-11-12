package com.majestic.food.api.majestic_food_api.entities.dtos;

import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.validation.IfExistsUpdate;

import jakarta.validation.constraints.NotBlank;

public class ProductCategoryUpdateDTO {

    @IfExistsUpdate(entity = ProductCategory.class, field = "categoryName", message = "{IfExists.category.name}")
    @NotBlank(message = "{NotBlank.validation.text}")
    private String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
