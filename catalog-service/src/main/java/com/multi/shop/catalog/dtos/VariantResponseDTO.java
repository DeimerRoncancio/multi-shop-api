package com.multi.shop.catalog.dtos;

import org.springframework.data.annotation.Transient;

import java.util.List;

public record VariantResponseDTO(
    String id,
    String name,
    String tag,
    String type,
    @Transient
    List<String> listValues
) {
}
