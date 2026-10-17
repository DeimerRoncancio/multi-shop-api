package com.multi.shop.transactions.repositories;

import com.multi.shop.transactions.entities.Address;
import com.multi.shop.transactions.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {
    Optional<Address> findByCustomerAndAddressName(Customer customer, String addressName);
    Optional<Address> findByIdAndCustomer_UserEmail(String id, String userEmail);
    List<Address> findByCustomer_UserEmailOrderByAddressNameAsc(String userEmail);
}
