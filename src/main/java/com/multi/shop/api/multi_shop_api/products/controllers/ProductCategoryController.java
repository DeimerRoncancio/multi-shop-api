package com.multi.shop.api.multi_shop_api.products.controllers;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductCategoryDTO;
import jakarta.validation.Valid;
import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RestController;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.dtos.CategoryResponseDTO;
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
    public Page<CategoryResponseDTO> viewAll(@PageableDefault Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> view(@PathVariable String id) {
        Optional<CategoryResponseDTO> opCategory = service.findOne(id);
        CategoryResponseDTO category = opCategory.orElseThrow(() -> new NotFoundException("Category nod fount"));

        return ResponseEntity.ok().body(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductCategoryDTO> create(@Valid @RequestBody ProductCategoryDTO category) {
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
