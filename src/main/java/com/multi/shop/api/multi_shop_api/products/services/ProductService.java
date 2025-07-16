package com.multi.shop.api.multi_shop_api.products.services;

import java.util.Optional;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.multi.shop.api.multi_shop_api.products.entities.Product;

public interface ProductService {
    Page<Product> findAll(Pageable pageable);

    Optional<Product> findOne(String id);

    ProductDTO save(ProductDTO product);

    Optional<ProductDTO> update(String id, ProductDTO product);

    Optional<Product> delete(String id);

    int productsSize();
}
