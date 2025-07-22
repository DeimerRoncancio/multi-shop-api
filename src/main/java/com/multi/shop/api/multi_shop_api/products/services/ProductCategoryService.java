package com.multi.shop.api.multi_shop_api.products.services;

import java.util.List;
import java.util.Optional;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.CategoryResponseDTO;
import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCategoryService {
    Page<CategoryResponseDTO> findAll(Pageable pageable);

    Optional<CategoryResponseDTO> findOne(String id);

    ProductCategoryDTO save(ProductCategoryDTO categoty);

    Optional<ProductCategoryDTO> update(String id, ProductCategoryDTO category);

    Optional<ProductCategory> delete(String id);

    List<ProductCategory> findCategoriesByName(List<String> categoryNames);

    int categoriesSize();
}
