package com.multi.shop.api.multi_shop_api.payments.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StripeRequestDTO(
    @NotBlank
    String currency,

    @NotEmpty
    @Valid
    List<StripeItemDTO> items
) {
}
