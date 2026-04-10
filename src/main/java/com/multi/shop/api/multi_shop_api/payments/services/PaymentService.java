package com.multi.shop.api.multi_shop_api.payments.services;

import com.multi.shop.api.multi_shop_api.payments.dtos.NewTransactionDTO;

public interface PaymentService {
    String createTransaction(NewTransactionDTO dto);
}
