package com.multi.shop.api.multi_shop_api.transactions.dtos;

public record CheckoutProductItemDTO(
    String id,
    String productName,
    Long price,
    int quantity
) {}
