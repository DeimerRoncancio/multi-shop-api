package com.multi.shop.transactions.dtos;

public record CheckoutProductItemDTO(
    String id,
    String productName,
    Long price,
    int quantity
) {}
