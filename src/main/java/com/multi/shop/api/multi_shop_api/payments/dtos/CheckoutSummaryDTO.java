package com.multi.shop.api.multi_shop_api.payments.dtos;

import com.multi.shop.api.multi_shop_api.payments.enums.TransactionStatus;

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
