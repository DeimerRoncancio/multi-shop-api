package com.multi.shop.api.multi_shop_api.payments.dtos;

public record StripeResponseDTO(
    String status,
    String message,
    String sessionId,
    String sessionUrl
) {
}
