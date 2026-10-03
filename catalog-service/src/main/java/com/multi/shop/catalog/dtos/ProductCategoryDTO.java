package com.multi.shop.catalog.dtos;

import com.multi.shop.catalog.common.validation.IfExists;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProductCategoryDTO(
    @IfExists(message = "{IfExists.validation}", field = "categoryName", entity = "ProductCategory")
    @NotBlank(message = "{NotBlank.validation.text}")
    String categoryName
) {
}
