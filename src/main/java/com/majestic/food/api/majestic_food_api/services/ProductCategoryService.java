package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductCategoryCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductCategoryUpdateDTO;

public interface ProductCategoryService {

    List<ProductCategory> findAll();

    Optional<ProductCategory> findOne(String id);

    ProductCategory save(ProductCategoryCreateDTO categoty);

    Optional<ProductCategory> update(String id, ProductCategoryUpdateDTO category);

    Optional<ProductCategory> delete(String id);
}
