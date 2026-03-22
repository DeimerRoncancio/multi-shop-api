package com.multi.shop.api.multi_shop_api.products.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VariantDTO(
    String id,

    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExists(field = "name", entity = "Variant")
    String name,

    @NotBlank
    String type,

    @NotBlank(message = "{NotBlank.validation.text}")
    String tag,

    @NotEmpty(message = "{NotEmpty.validation.list}")
    List<String> listValues
) {
}
