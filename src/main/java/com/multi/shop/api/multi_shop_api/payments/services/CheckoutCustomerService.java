package com.multi.shop.api.multi_shop_api.payments.services;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface CheckoutCustomerService {
    Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId, String checkoutAccessToken, String userIdentity);
    List<CustomerAddressDTO> getSavedAddresses(String userIdentity);
}
