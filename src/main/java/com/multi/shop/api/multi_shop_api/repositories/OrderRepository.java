package com.multi.shop.api.multi_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.api.multi_shop_api.entities.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
}
