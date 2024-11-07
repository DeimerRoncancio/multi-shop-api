package com.majestic.food.api.majestic_food_api.controllers;

import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.services.ProductCategoryService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/categories")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService service;

    @GetMapping
    public List<ProductCategory> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> findOne(@PathVariable String id) {
        Optional<ProductCategory> optionalCategory = service.findOne(id);

        if (optionalCategory.isPresent())
            return ResponseEntity.ok().body(optionalCategory.get());
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProductCategory> create(@RequestBody ProductCategory category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategory> update(@PathVariable String id, @RequestBody ProductCategory category) {
        Optional<ProductCategory> categoryDb = service.update(id, category);

        if (categoryDb.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryDb.get());
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<ProductCategory> categoryDb = service.delete(id);

        if (categoryDb.isPresent())
            return ResponseEntity.ok().build();
        
        return ResponseEntity.notFound().build();
    }
}
