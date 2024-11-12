package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Order;
import com.majestic.food.api.majestic_food_api.entities.dtos.OrderCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.OrderUpdateDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findOne(String id);

    Order save(OrderCreateDTO user);

    Optional<Order> update(String id, OrderUpdateDTO order);

    Optional<Order> delete(String id);
}
