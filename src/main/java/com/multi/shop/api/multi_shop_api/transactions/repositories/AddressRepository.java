package com.multi.shop.api.multi_shop_api.transactions.repositories;

import com.multi.shop.api.multi_shop_api.transactions.entities.Address;
import com.multi.shop.api.multi_shop_api.transactions.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {
    Optional<Address> findByCustomerAndAddressName(Customer customer, String addressName);
}
