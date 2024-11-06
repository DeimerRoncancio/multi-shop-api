package com.majestic.food.api.majestic_food_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.majestic.food.api.majestic_food_api.entities.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, String> {

}
