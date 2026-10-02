package com.multi.shop.api.multi_shop_api.transactions.dtos;

public record TransactionAccessDTO(
    String transactionId,
    String checkoutAccessToken
) {}
