package com.multi.shop.api.multi_shop_api.entities.dtos;

import com.multi.shop.api.multi_shop_api.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.validation.IfExists;

import jakarta.validation.constraints.NotBlank;

public class NewProductCategoryDTO {

    @IfExists(entity = ProductCategory.class, field = "categoryName", message = "{IfExists.category.name}")
    @NotBlank(message = "{NotBlank.validation.text}")
    private String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
