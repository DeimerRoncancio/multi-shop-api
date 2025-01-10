package com.multi.shop.api.multi_shop_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.api.multi_shop_api.entities.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, String> {

    List<ProductCategory> findByCategoryNameIn(List<String> categories);
}
