package com.multi.shop.api.multi_shop_api.payments.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductItemDTO(
    @NotBlank
    String id,

    @Min(1)
    int quantity
) {}
