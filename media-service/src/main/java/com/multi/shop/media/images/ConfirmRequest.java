package com.multi.shop.media.images;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConfirmRequest(
    @NotNull
    List<String> ids
) {
}
