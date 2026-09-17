package com.multi.shop.api.multi_shop_api.payments.services;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerCheckoutDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;

import java.util.Optional;

public interface CheckoutCustomerService {
    Optional<Customer> getCustomer(String transactionId);
    Optional<CustomerCheckoutDTO> getCheckoutCustomer(String transactionId, String email);
    Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId);
}
