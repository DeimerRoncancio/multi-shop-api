package com.multi.shop.catalog.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.multi.shop.catalog.dtos.HomeDTO;
import com.multi.shop.catalog.dtos.ProductDTO;
import com.multi.shop.catalog.dtos.ProductResponseDTO;
import com.multi.shop.catalog.dtos.ProductSuggestionsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.multi.shop.catalog.entities.Product;

public interface ProductService {
    Page<ProductResponseDTO> findAll(Pageable pageable);

    Optional<ProductResponseDTO> findOne(String id);

    ProductDTO save(ProductDTO product);

    Optional<ProductDTO> update(String id, ProductDTO product);

    Optional<Product> delete(String id);

    Page<ProductResponseDTO> search(String query, List<String> category, Long minPrice, Long maxPrice, Pageable pageable);

    ProductSuggestionsDTO suggestions(List<String> categories, List<String> exclude, int size, Integer min, Sort sort);

    HomeDTO home();

    List<ProductResponseDTO> findByIds(List<String> ids);

    Long productsSize();

    List<ProductResponseDTO> latestProducts();

    Map<String, Long> productsStats();
}
