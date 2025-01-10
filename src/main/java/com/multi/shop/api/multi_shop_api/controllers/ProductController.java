package com.multi.shop.api.multi_shop_api.controllers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.entities.Product;
import com.multi.shop.api.multi_shop_api.entities.dtos.NewProductDTO;
import com.multi.shop.api.multi_shop_api.entities.dtos.UpdateProductDTO;
import com.multi.shop.api.multi_shop_api.services.ProductService;
import com.multi.shop.api.multi_shop_api.validation.FilesValidation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/app/products")
@CrossOrigin(originPatterns = "*")
public class ProductController {

    private final ProductService service;
    private final FilesValidation validation;

    public ProductController(ProductService service, FilesValidation validation) {
        this.service = service;
        this.validation = validation;
    }

    @GetMapping
    public List<Product> viewAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> view(@PathVariable String id) {
        Optional<Product> productDb = service.findOne(id);

        if (productDb.isPresent())
            return ResponseEntity.ok().body(productDb.get());
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @ModelAttribute NewProductDTO product, BindingResult result, 
    @RequestPart List<MultipartFile> files) {
        validation.validate(files, result);
        if (result.hasFieldErrors())
            return validate(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(product, files));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@Valid @ModelAttribute UpdateProductDTO product, BindingResult result, 
    @PathVariable String id, @RequestPart List<MultipartFile> files) {
        validation.validate(files, result);
        if (result.hasFieldErrors())
            return validate(result);
        
        Optional<Product> productDb = service.update(id, product, files);

        if (productDb.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<Product> productDb = service.delete(id);

        if (productDb.isPresent())
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
