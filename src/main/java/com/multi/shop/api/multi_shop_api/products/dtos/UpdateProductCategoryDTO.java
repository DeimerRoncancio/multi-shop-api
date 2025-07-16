package com.multi.shop.api.multi_shop_api.products.dtos;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.common.validation.IfExistsValid;
import jakarta.validation.constraints.NotBlank;

public record UpdateProductCategoryDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExistsValid(message = "{IfExistsValid.category.name}", entity = ProductCategory.class, field = "categoryName")
    String categoryName
) {
}
