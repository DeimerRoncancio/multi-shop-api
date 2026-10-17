package com.multi.shop.transactions.dtos;

import com.multi.shop.transactions.enums.TransactionStatus;

import java.time.Instant;
import java.util.List;

public record PurchaseDTO(
    String id,
    TransactionStatus status,
    Instant date,
    Long totalPrice,
    CustomerAddressDTO shippingAddress,
    List<CheckoutProductItemDTO> items
) {}
