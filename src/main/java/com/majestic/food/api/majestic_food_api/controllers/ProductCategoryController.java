package com.majestic.food.api.majestic_food_api.controllers;

import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.services.ProductCategoryService;

import jakarta.validation.Valid;

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
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/app/categories")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService service;

    @GetMapping
    public List<ProductCategory> viewdAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> view(@PathVariable String id) {
        Optional<ProductCategory> optionalCategory = service.findOne(id);

        if (optionalCategory.isPresent())
            return ResponseEntity.ok().body(optionalCategory.get());
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProductCategory category, BindingResult result) {
        if (result.hasFieldErrors())
            return validate(result);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody ProductCategory category, BindingResult result, @PathVariable String id) {
        if (result.hasFieldErrors())
            return validate(result);
        
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

    public ResponseEntity<?> validate(BindingResult result) {
        Map<String, String> errors = new HashMap<> ();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }
}
