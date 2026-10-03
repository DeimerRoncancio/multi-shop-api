package com.multi.shop.media.images;

import jakarta.validation.constraints.NotBlank;

public record RenameRequest(
    @NotBlank
    String name
) {
}
