package com.multi.shop.identity.users.dtos;

import com.multi.shop.identity.auth.validation.SizeConstraint;
import jakarta.validation.constraints.NotBlank;

public record PasswordDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    @SizeConstraint(min = 8, max = 255)
    String currentPassword,

    @NotBlank(message = "{NotBlank.validation.text}")
    @SizeConstraint(min = 8, max = 255)
    String newPassword
) {
}
