package com.multi.shop.api.multi_shop_api.transactions.repositories;

import com.multi.shop.api.multi_shop_api.transactions.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentsRepository extends JpaRepository<Transaction, String> {
}
