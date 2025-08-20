package com.multi.shop.api.multi_shop_api.products.dtos;

import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Transient;

import java.util.List;

public record VariantDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExists(field = "name", entity = "Variant")
    String name,

    @NotBlank(message = "{NotBlank.validation.text}")
    String tag,

    @Transient
    @NotEmpty(message = "{NotEmpty.validation.list}")
    List<String> listValues
) {
}
