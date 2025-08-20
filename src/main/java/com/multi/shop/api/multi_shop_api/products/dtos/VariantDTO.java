package com.multi.shop.api.multi_shop_api.products.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Transient;

import java.util.List;

public record VariantDTO(
    String name,
    String tag,
    @Transient
    List<String> listValues
) {
}
