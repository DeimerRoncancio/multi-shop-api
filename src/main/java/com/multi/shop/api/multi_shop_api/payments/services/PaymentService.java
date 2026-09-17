package com.multi.shop.api.multi_shop_api.payments.services;

import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.TransactionAccessDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Optional<CheckoutSummaryDTO> getCheckoutSummary(String transactionId, String checkoutAccessToken);
    TransactionAccessDTO createTransaction(NewTransactionDTO dto);
    Optional<Transaction> updateProducts(String id, List<ProductItemDTO> products);
    Optional<Transaction> deleteTransaction(String id);
}
