package com.multi.shop.api.multi_shop_api.catalog.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.media.api.StoredImage;

import java.util.List;

public record ProductItemDTO(
        String id,
        String productName,
        Long price,

        @JsonIgnoreProperties("id")
        StoredImage mainImage
) {
}
