package com.multi.shop.api.multi_shop_api.payments.services;

import com.multi.shop.api.multi_shop_api.payments.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;

import java.util.Optional;

public interface PaymentService {
    String createTransaction(NewTransactionDTO dto);
    Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId);
}
