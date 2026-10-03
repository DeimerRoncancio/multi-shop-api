package com.multi.shop.transactions.dtos;

import com.multi.shop.transactions.enums.TransactionStatus;

import java.util.List;

public record CheckoutSummaryDTO(
    String transactionId,
    TransactionStatus status,
    Long totalPrice,
    CustomerSummaryDTO customer,
    List<CustomerAddressDTO> addresses,
    CustomerAddressDTO selectedAddress,
    List<CheckoutProductItemDTO> items
) {}
