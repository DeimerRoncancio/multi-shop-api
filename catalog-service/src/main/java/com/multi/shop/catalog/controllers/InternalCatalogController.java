package com.multi.shop.catalog.controllers;

import com.multi.shop.catalog.entities.Product;
import com.multi.shop.catalog.repositories.ProductCategoryRepository;
import com.multi.shop.catalog.repositories.ProductRepository;
import com.multi.shop.catalog.services.CatalogProduct;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/internal")
public class InternalCatalogController {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public InternalCatalogController(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/products/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<CatalogProduct> findProduct(@PathVariable String id) {
        return productRepository.findById(id)
            .map(InternalCatalogController::toCatalogProduct)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/products")
    @Transactional(readOnly = true)
    public List<CatalogProduct> findProducts(@RequestParam(name = "ids", required = false) List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        return productRepository.findAllById(ids.stream().filter(Objects::nonNull).distinct().toList()).stream()
            .map(InternalCatalogController::toCatalogProduct)
            .toList();
    }

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public Map<String, Long> stats() {
        return Map.of(
            "products", productRepository.count(),
            "categories", categoryRepository.count()
        );
    }

    private static CatalogProduct toCatalogProduct(Product product) {
        return new CatalogProduct(product.getId(), product.getProductName(), product.getDescription(), product.getPrice());
    }
}
