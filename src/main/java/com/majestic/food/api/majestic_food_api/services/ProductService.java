package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.entities.Product;

public interface ProductService {

    List<Product> findAll();

    Optional<Product> findOne(String id);

    Product save(Product product);

    Optional<Product> update(String id, Product product);

    Optional<Product> delete(String id);
}
