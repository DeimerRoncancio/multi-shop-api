package com.multi.shop.transactions.repositories;

import com.multi.shop.transactions.entities.Customer;
import com.multi.shop.transactions.entities.Transaction;
import com.multi.shop.transactions.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentsRepository extends JpaRepository<Transaction, String> {
    boolean existsByCustomer(Customer customer);
    List<Transaction> findByCustomer_UserEmailIgnoreCaseAndStatusNotOrderByCreatedAtDesc(String email, TransactionStatus status);
    List<Transaction> findByCustomer_Guest_UserEmailIgnoreCaseAndStatusNotOrderByCreatedAtDesc(String email, TransactionStatus status);
}
