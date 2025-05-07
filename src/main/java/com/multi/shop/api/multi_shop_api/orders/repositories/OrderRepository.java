package com.multi.shop.api.multi_shop_api.orders.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.multi.shop.api.multi_shop_api.orders.entities.Order;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderName(String orderName);
}
