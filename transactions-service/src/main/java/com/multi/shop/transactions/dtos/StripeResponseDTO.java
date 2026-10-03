package com.multi.shop.transactions.dtos;

public record StripeResponseDTO(
    String status,
    String message,
    String sessionId,
    String sessionUrl
) {
}
