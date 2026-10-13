package com.multi.shop.catalog.dtos;

import java.util.List;

public record ProductSuggestionsDTO(
    List<ProductResponseDTO> products,
    boolean filled
) {
}
