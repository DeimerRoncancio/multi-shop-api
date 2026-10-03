package com.multi.shop.transactions.repositories;

import com.multi.shop.transactions.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentsRepository extends JpaRepository<Transaction, String> {
}
