package com.majestic.food.api.majestic_food_api.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.majestic.food.api.majestic_food_api.entities.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {

}
