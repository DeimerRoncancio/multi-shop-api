package com.multi.shop.api.multi_shop_api.products.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductResponseDTO;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.services.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
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
    public Page<ProductResponseDTO> viewAll(
    @PageableDefault(sort = "productName", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> view(@PathVariable("id") String id) throws JsonProcessingException {
        Optional<ProductResponseDTO> productDb = service.findOne(id);
        ProductResponseDTO product = productDb.orElseThrow(() -> new NotFoundException("Product not found"));

        return ResponseEntity.ok().body(product);
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
    @PathVariable("id") String id) {
        Optional<ProductDTO> productDb = service.update(id, productDTO);
        ProductDTO product = productDb.orElseThrow(() -> new NotFoundException("Product not found"));

        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        Optional<Product> productDb = service.delete(id);
        productDb.orElseThrow(() -> new NotFoundException("Product not found"));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public Page<ProductResponseDTO> searchProducts(
    @PageableDefault Pageable pageable, @RequestParam(value = "query", required = false) String query,
    @RequestParam(value = "categories", required = false) List<String> categories) {
        return service.search(query, categories, pageable);
    }

    @GetMapping("/latest-products")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponseDTO> findLastCreated() {
        return service.latestProducts();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> productStats() {
        return ResponseEntity.ok().body(service.productsStats());
    }
}
