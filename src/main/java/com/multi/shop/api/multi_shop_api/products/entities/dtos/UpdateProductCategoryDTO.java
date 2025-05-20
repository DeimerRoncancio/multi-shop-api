package com.multi.shop.api.multi_shop_api.products.entities.dtos;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.validation.IfExists;
import com.multi.shop.api.multi_shop_api.validation.IfExistsUpdate;
import jakarta.validation.constraints.NotBlank;

public class UpdateProductCategoryDTO {
    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExists(message = "{IfExists.category.name}", entity = ProductCategory.class, field = "categoryName")
    private String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
