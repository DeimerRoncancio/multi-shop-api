package com.multi.shop.api.multi_shop_api.services;

import java.util.List;
import java.util.Optional;

import com.multi.shop.api.multi_shop_api.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.entities.dtos.NewProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.entities.dtos.UpdateProductCategoryDTO;

public interface ProductCategoryService {

    List<ProductCategory> findAll();

    Optional<ProductCategory> findOne(String id);

    NewProductCategoryDTO save(NewProductCategoryDTO categoty);

    Optional<ProductCategory> update(String id, UpdateProductCategoryDTO category);

    Optional<ProductCategory> delete(String id);

    List<ProductCategory> findCategoriesByName(List<String> categoryNames);
}
