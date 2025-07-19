package com.multi.shop.api.multi_shop_api.products.controllers;

import jakarta.validation.Valid;
import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import org.springframework.web.bind.annotation.RestController;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/categories")
@CrossOrigin(originPatterns = "*")
public class ProductCategoryController {
    private final ProductCategoryService service;

    public ProductCategoryController(ProductCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductCategory> viewAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> view(@PathVariable String id) {
        Optional<ProductCategory> opCategory = service.findOne(id);

        return opCategory.map(category ->ResponseEntity.ok().body(category))
            .orElseGet(() ->ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody ProductCategoryDTO category) {
        ProductCategoryDTO newCategory = service.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductCategoryDTO> update(@Valid @RequestBody ProductCategoryDTO categoryDTO,
    @PathVariable String id) {
        Optional<ProductCategoryDTO> categoryDb = service.update(id, categoryDTO);
        ProductCategoryDTO category = categoryDb.orElseThrow(() -> new NotFoundException("Category not found"));

        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Optional<ProductCategory> categoryDb = service.delete(id);
        categoryDb.orElseThrow(() -> new NotFoundException("Category not found"));

        return ResponseEntity.ok().build();
    }
}
