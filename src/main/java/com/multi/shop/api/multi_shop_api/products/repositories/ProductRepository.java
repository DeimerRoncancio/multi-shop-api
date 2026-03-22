package com.multi.shop.api.multi_shop_api.products.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("""
        SELECT DISTINCT p
        FROM Product p
        JOIN p.categories c
        WHERE (?1 IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', ?1, '%')))
        AND (?2 IS NULL OR c.categoryName IN ?2)
    """)
    Page<Product> findByProductNameOrCategories(String query, List<String> categories, Pageable pageable);

    List<Product> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(DISTINCT p) FROM Product p JOIN p.variants v")
    long countProductsWithVariants();
}
