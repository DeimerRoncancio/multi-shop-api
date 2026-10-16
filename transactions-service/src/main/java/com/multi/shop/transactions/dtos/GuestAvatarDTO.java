package com.multi.shop.transactions.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GuestAvatarDTO(
    @NotBlank
    @Email
    String email,

    @NotNull
    @Min(1)
    @Max(5)
    Integer avatar
) {}
