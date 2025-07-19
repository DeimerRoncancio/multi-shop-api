package com.multi.shop.api.multi_shop_api.products.controllers;

import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.services.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

@RestController
@RequestMapping("/app/products")
@CrossOrigin(originPatterns = "*")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Product> viewAll(@PageableDefault Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> view(@PathVariable String id) {
        Optional<Product> productDb = service.findOne(id);

        return productDb.map(product -> ResponseEntity.ok().body(product))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> create(@ModelAttribute @Valid ProductDTO product) {
        ProductDTO newProduct = service.save(product);
        return ResponseEntity.ok().body(newProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> update(@ModelAttribute @Valid ProductDTO productDTO,
    @PathVariable String id) {
        Optional<ProductDTO> productDb = service.update(id, productDTO);
        ProductDTO product = productDb.orElseThrow(() -> new NotFoundException("Product not found"));

        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Optional<Product> productDb = service.delete(id);
        productDb.orElseThrow(() -> new NotFoundException("Product not found"));

        return ResponseEntity.ok().build();
    }
}
