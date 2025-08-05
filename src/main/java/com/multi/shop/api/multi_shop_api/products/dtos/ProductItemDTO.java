package com.multi.shop.api.multi_shop_api.products.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.images.entities.Image;

import java.util.List;

public record ProductItemDTO(
        String id,
        String productName,
        Long price,

        @JsonIgnoreProperties("id")
        Image mainImage
) {
}
