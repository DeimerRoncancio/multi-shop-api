package com.multi.shop.api.multi_shop_api.products.dtos;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;

import jakarta.validation.constraints.NotBlank;

public record NewProductCategoryDTO(
    @IfExists(entity = ProductCategory.class, field = "categoryName", message = "{IfExists.category.name}")
    @NotBlank(message = "{NotBlank.validation.text}")
    String categoryName
) {
}
