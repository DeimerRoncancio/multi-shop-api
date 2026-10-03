package com.multi.shop.transactions.dtos;

public record TransactionAccessDTO(
    String transactionId,
    String checkoutAccessToken
) {}
