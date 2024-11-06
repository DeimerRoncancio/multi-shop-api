package com.majestic.food.api.majestic_food_api.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.majestic.food.api.majestic_food_api.entities.ProductType;

public interface ProductTypeRepository extends JpaRepository<ProductType, UUID> {

}
