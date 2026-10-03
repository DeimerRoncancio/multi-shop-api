package com.multi.shop.catalog.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.catalog.entities.Product;

import java.util.List;

public record CategoryResponseDTO(
    String id,
    String categoryName,
    @JsonIgnoreProperties("categories")
    List<ProductItemDTO> products
) {
}
