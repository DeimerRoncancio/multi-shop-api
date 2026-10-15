package com.multi.shop.catalog.dtos;

import java.util.List;

public record SearchFacetsDTO(
    Long minPrice,
    Long maxPrice,
    List<CategoryCountDTO> categories
) {}
