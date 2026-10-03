package com.multi.shop.transactions.repositories;

import com.multi.shop.transactions.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomersRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByUserEmail(String email);
    Optional<Customer> findFirstByGuest_UserNamesAndGuest_UserEmailAndGuest_UserPhone(String userNames, String userEmail, String userPhone);
}
