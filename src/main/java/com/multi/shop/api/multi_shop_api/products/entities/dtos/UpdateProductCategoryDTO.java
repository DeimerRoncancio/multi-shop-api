package com.multi.shop.api.multi_shop_api.products.entities.dtos;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.validation.IfExistsUpdate;

import jakarta.validation.constraints.NotBlank;

public class UpdateProductCategoryDTO {
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
