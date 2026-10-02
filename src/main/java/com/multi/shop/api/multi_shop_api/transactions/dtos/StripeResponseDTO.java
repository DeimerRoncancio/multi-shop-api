package com.multi.shop.api.multi_shop_api.transactions.dtos;

public record StripeResponseDTO(
    String status,
    String message,
    String sessionId,
    String sessionUrl
) {
}
