package com.multi.shop.transactions.services;

import com.multi.shop.transactions.dtos.CustomerAddressDTO;
import com.multi.shop.transactions.dtos.UserTransactionDTO;
import com.multi.shop.transactions.entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface CheckoutCustomerService {
    Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId, String checkoutAccessToken, String userIdentity);
    List<CustomerAddressDTO> getSavedAddresses(String userIdentity);
}
