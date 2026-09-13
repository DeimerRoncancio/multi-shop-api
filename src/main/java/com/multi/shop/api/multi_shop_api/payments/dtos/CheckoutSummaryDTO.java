package com.multi.shop.api.multi_shop_api.payments.dtos;

import java.util.List;

public record CheckoutSummaryDTO(
    String transactionId,
    String status,
    Long totalPrice,
    CustomerSummaryDTO customer,
    List<CustomerAddressDTO> addresses,
    CustomerAddressDTO selectedAddress,
    List<CheckoutProductItemDTO> items
) {}
