package com.multi.shop.api.multi_shop_api.products.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, String> {
    Optional<ProductCategory> findByCategoryName(String categoryName);
    List<ProductCategory> findByCategoryNameIn(List<String> categories);
}
