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
        SELECT p
        FROM Product p
        WHERE (?1 IS NULL
            OR LOWER(p.productName) LIKE LOWER(CONCAT('%', ?1, '%'))
            OR EXISTS (
                SELECT 1 FROM Product p1 JOIN p1.categories c1
                WHERE p1 = p AND LOWER(c1.categoryName) LIKE LOWER(CONCAT('%', ?1, '%'))))
        AND (?2 IS NULL OR EXISTS (
            SELECT 1 FROM Product p2 JOIN p2.categories c2
            WHERE p2 = p AND c2.categoryName IN ?2))
        AND (?3 IS NULL OR p.price >= ?3)
        AND (?4 IS NULL OR p.price <= ?4)
    """)
    Page<Product> findByProductNameOrCategories(
        String query, List<String> categories, Long minPrice, Long maxPrice, Pageable pageable);

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

    List<Product> findByCategoriesId(String categoryId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT p) FROM Product p JOIN p.variants v")
    long countProductsWithVariants();
}
