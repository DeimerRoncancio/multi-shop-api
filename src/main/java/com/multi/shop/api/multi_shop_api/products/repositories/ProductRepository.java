package com.multi.shop.api.multi_shop_api.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.api.multi_shop_api.products.entities.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
}
