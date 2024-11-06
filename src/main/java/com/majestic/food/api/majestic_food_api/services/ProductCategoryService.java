package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.entities.ProductCategory;

public interface ProductCategoryService {

    List<ProductCategory> findAll();

    Optional<ProductCategory> findOne(String id);
}
