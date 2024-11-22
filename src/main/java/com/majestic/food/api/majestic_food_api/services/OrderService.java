package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Order;
import com.majestic.food.api.majestic_food_api.entities.dtos.NewOrderDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.UpdateOrderDTO;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findOne(String id);

    Order save(NewOrderDTO user);

    Optional<Order> update(String id, UpdateOrderDTO order);

    Optional<Order> delete(String id);
}
