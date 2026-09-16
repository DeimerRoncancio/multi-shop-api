package com.multi.shop.api.multi_shop_api.payments.dtos;

public record TransactionAccessDTO(
    String transactionId,
    String checkoutAccessToken
) {}
