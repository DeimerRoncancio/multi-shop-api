package com.multi.shop.api.multi_shop_api.payments.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserTransactionDTO(
    @NotBlank
    String userNames,

    @NotBlank
    @Email
    String userEmail,

    @NotBlank
    String userPhone,

    @NotNull
    @Valid
    CustomerAddressDTO userAddress
) {}
