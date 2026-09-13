package com.multi.shop.api.multi_shop_api.payments.services;

import com.multi.shop.api.multi_shop_api.payments.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerCheckoutDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Optional<Customer> getCustomer(String transactionId);
    Optional<CustomerCheckoutDTO> getCheckoutCustomer(String transactionId, String email);
    Optional<CheckoutSummaryDTO> getCheckoutSummary(String transactionId);
    String createTransaction(NewTransactionDTO dto);
    Optional<Transaction> updateProducts(String id, List<ProductItemDTO> products);
    void addTransactionDate(String transactionId, Date date);
    Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId);
    void setStatus(String transactionId, String status);
    Optional<Transaction> deleteTransaction(String id);
}
