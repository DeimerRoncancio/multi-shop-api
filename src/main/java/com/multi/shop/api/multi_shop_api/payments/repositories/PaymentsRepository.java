package com.multi.shop.api.multi_shop_api.payments.repositories;

import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentsRepository extends JpaRepository<Transaction, String> {
}
