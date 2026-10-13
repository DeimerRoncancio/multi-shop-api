package com.multi.shop.catalog.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.multi.shop.catalog.entities.Product;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("""
        SELECT DISTINCT p
        FROM Product p
        JOIN p.categories c
        WHERE (?1 IS NULL
            OR LOWER(p.productName) LIKE LOWER(CONCAT('%', ?1, '%'))
            OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', ?1, '%')))
        AND (?2 IS NULL OR c.categoryName IN ?2)
    """)
    Page<Product> findByProductNameOrCategories(String query, List<String> categories, Pageable pageable);

    @Query("""
        SELECT DISTINCT p
        FROM Product p
        JOIN p.categories c
        WHERE c.categoryName IN ?1
        AND p.id NOT IN ?2
    """)
    List<Product> findByCategoriesExcluding(List<String> categories, List<String> exclude, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id NOT IN ?1")
    List<Product> findExcluding(List<String> exclude, Pageable pageable);

    List<Product> findTop5ByOrderByCreatedAtDesc();

    List<Product> findTop4ByCategoriesIdOrderByIdAsc(String categoryId);

    @Query("SELECT COUNT(DISTINCT p) FROM Product p JOIN p.variants v")
    long countProductsWithVariants();
}
