package com.multi.shop.identity.users.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvatarDTO(
    @NotNull
    @Min(1)
    @Max(5)
    Integer avatar
) {
}
