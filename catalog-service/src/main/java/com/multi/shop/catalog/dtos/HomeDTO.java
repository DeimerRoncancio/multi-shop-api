package com.multi.shop.catalog.dtos;

import java.util.List;

public record HomeDTO(
    List<HomeSectionDTO> sections,
    List<ProductResponseDTO> top,
    List<ProductResponseDTO> budget
) {
}
