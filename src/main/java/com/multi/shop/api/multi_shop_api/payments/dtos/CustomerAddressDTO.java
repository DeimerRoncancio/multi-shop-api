package com.multi.shop.api.multi_shop_api.payments.dtos;

import jakarta.validation.constraints.NotBlank;

public record CustomerAddressDTO(
    @NotBlank
    String addressName,

    @NotBlank
    String address,

    @NotBlank
    String city,

    @NotBlank
    String state,

    @NotBlank
    String country,

    String addressNumber
) {}
