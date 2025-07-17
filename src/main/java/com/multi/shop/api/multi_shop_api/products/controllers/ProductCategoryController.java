package com.multi.shop.api.multi_shop_api.products.controllers;

import com.multi.shop.api.multi_shop_api.products.repositories.ProductCategoryRepository;
import jakarta.validation.Valid;

import org.springframework.validation.ObjectError;
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
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        return opCategory.map(category ->
            ResponseEntity.ok().body(category)
        ).orElseGet(() ->ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody ProductCategoryDTO category) {
        ProductCategoryDTO newCategory = service.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@Valid @RequestBody ProductCategoryDTO category, @PathVariable String id) {
        Optional<ProductCategory> categoryDb = service.update(id, category);

        if (categoryDb.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<ProductCategory> categoryDb = service.delete(id);

        if (categoryDb.isPresent())
            return ResponseEntity.ok().build();
        
        return ResponseEntity.notFound().build();
    }
}
