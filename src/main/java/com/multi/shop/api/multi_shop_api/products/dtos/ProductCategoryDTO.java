package com.multi.shop.api.multi_shop_api.products.dtos;

import com.multi.shop.api.multi_shop_api.common.validation.IfExists;

import jakarta.validation.constraints.NotBlank;

public record ProductCategoryDTO(
    @IfExists(message = "{IfExists.validation}", field = "categoryName", entity = "ProductCategory")
    @NotBlank(message = "{NotBlank.validation.text}")
    String categoryName
) {
}
