package com.majestic.food.api.majestic_food_api.repositories;

import org.springframework.data.repository.CrudRepository;

import com.majestic.food.api.majestic_food_api.entities.Product;

public interface ProductRepository extends CrudRepository<String, Product> {

}
