package com.multi.shop.transactions.services;

import com.multi.shop.transactions.dtos.CheckoutSummaryDTO;
import com.multi.shop.transactions.dtos.NewTransactionDTO;
import com.multi.shop.transactions.dtos.ProductItemDTO;
import com.multi.shop.transactions.dtos.TransactionAccessDTO;
import com.multi.shop.transactions.entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Optional<CheckoutSummaryDTO> getCheckoutSummary(String transactionId, String checkoutAccessToken);
    TransactionAccessDTO createTransaction(NewTransactionDTO dto);
    Optional<Transaction> updateProducts(String id, List<ProductItemDTO> products, String checkoutAccessToken);
    Optional<Transaction> deleteTransaction(String id, String checkoutAccessToken);
}
