package com.multi.shop.api.multi_shop_api.catalog.services.impl;

import com.multi.shop.api.multi_shop_api.catalog.api.CatalogApi;
import com.multi.shop.api.multi_shop_api.catalog.api.CatalogProduct;
import com.multi.shop.api.multi_shop_api.catalog.entities.Product;
import com.multi.shop.api.multi_shop_api.catalog.repositories.ProductCategoryRepository;
import com.multi.shop.api.multi_shop_api.catalog.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CatalogApiImpl implements CatalogApi {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public CatalogApiImpl(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CatalogProduct> findProduct(String id) {
        if (id == null) return Optional.empty();
        return productRepository.findById(id).map(CatalogApiImpl::toCatalogProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, CatalogProduct> findProducts(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();

        return productRepository.findAllById(ids.stream().filter(Objects::nonNull).distinct().toList()).stream()
            .map(CatalogApiImpl::toCatalogProduct)
            .collect(Collectors.toMap(CatalogProduct::id, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCategories() {
        return categoryRepository.count();
    }

    private static CatalogProduct toCatalogProduct(Product product) {
        return new CatalogProduct(product.getId(), product.getProductName(), product.getDescription(), product.getPrice());
    }
}
