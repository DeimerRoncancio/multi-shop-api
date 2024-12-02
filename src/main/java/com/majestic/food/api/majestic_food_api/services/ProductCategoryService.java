package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.entities.dtos.NewProductCategoryDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.UpdateProductCategoryDTO;

public interface ProductCategoryService {

    List<ProductCategory> findAll();

    Optional<ProductCategory> findOne(String id);

    NewProductCategoryDTO save(NewProductCategoryDTO categoty);

    Optional<ProductCategory> update(String id, UpdateProductCategoryDTO category);

    Optional<ProductCategory> delete(String id);
}
