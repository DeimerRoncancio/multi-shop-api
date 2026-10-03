package com.multi.shop.catalog.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.catalog.media.StoredImage;

import java.util.List;

public record ProductItemDTO(
        String id,
        String productName,
        Long price,

        @JsonIgnoreProperties("id")
        StoredImage mainImage
) {
}
