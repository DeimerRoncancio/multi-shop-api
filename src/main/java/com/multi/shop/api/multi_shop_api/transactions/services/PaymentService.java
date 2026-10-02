package com.multi.shop.api.multi_shop_api.transactions.services;

import com.multi.shop.api.multi_shop_api.transactions.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.transactions.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.transactions.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.transactions.dtos.TransactionAccessDTO;
import com.multi.shop.api.multi_shop_api.transactions.entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Optional<CheckoutSummaryDTO> getCheckoutSummary(String transactionId, String checkoutAccessToken);
    TransactionAccessDTO createTransaction(NewTransactionDTO dto);
    Optional<Transaction> updateProducts(String id, List<ProductItemDTO> products, String checkoutAccessToken);
    Optional<Transaction> deleteTransaction(String id, String checkoutAccessToken);
}
