package com.multi.shop.api.multi_shop_api.orders.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.api.multi_shop_api.orders.entities.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
}
