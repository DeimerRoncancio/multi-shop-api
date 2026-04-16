package com.multi.shop.api.multi_shop_api.payments.dtos;

public record UserTransactionDTO(
    String userId,
    String userNames,
    String userEmail,
    String userPhone,
    String userAddress
) {}
