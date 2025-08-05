package com.multi.shop.api.multi_shop_api.products.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProductCategoryDTO(
    @IfExists(message = "{IfExists.validation}", field = "categoryName", entity = "ProductCategory")
    @NotBlank(message = "{NotBlank.validation.text}")
    String categoryName,
    List<String> productList
) {
}
