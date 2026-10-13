package com.multi.shop.catalog.dtos;

import java.util.List;

public record CategorySummaryDTO(
    String id,
    String categoryName,
    long productCount,
    Long fromPrice,
    List<ProductItemDTO> products
) {}
