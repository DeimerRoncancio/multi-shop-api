package com.majestic.food.api.majestic_food_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.majestic.food.api.majestic_food_api.entities.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
}
