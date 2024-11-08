package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findOne(String id);

    Order save(Order user);

    Optional<Order> update(String id, Order order);

    Optional<Order> delete(String id);

    boolean existsByOrderName(String name);
}
