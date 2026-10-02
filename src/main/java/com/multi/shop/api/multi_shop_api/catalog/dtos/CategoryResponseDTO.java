package com.multi.shop.api.multi_shop_api.catalog.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.catalog.entities.Product;

import java.util.List;

public record CategoryResponseDTO(
    String id,
    String categoryName,
    @JsonIgnoreProperties("categories")
    List<ProductItemDTO> products
) {
}
