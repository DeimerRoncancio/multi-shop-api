package com.multi.shop.catalog.dtos;

import java.util.List;

public record HomeSectionDTO(
    String categoryName,
    long productCount,
    List<ProductResponseDTO> products
) {
}
