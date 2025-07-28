package com.multi.shop.api.multi_shop_api.payments.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StripeItemDTO(
    @NotBlank
    String name,

    @NotNull
    @PositiveOrZero
    Long price,

    @NotNull
    @PositiveOrZero
    Long quantity
) {
}
