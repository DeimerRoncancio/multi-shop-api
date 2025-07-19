package com.multi.shop.api.multi_shop_api.products.services;

import java.util.List;
import java.util.Optional;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;

public interface ProductCategoryService {
    List<ProductCategory> findAll();

    Optional<ProductCategory> findOne(String id);

    ProductCategoryDTO save(ProductCategoryDTO categoty);

    Optional<ProductCategoryDTO> update(String id, ProductCategoryDTO category);

    Optional<ProductCategory> delete(String id);

    List<ProductCategory> findCategoriesByName(List<String> categoryNames);

    int categoriesSize();
}
