package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.entities.Product;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductUpdateDTO;

public interface ProductService {

    List<Product> findAll();

    Optional<Product> findOne(String id);

    Product save(ProductCreateDTO product);

    Optional<Product> update(String id, ProductUpdateDTO product);

    Optional<Product> delete(String id);
}
