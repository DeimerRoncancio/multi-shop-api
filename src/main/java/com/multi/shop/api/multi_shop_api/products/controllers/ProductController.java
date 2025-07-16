package com.multi.shop.api.multi_shop_api.products.controllers;

import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.dtos.NewProductDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.UpdateProductDTO;
import com.multi.shop.api.multi_shop_api.products.services.ProductService;
import com.multi.shop.api.multi_shop_api.common.validation.validators.FileSizeValidation;
import com.multi.shop.api.multi_shop_api.products.validation.IfExistsCategoriesValidation;
import com.multi.shop.api.multi_shop_api.products.validation.MultipleFilesValidation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;

@RestController
@RequestMapping("/app/products")
@CrossOrigin(originPatterns = "*")
public class ProductController {
    private final ProductService service;
    private final ProductRepository repository;
    private final MultipleFilesValidation multipleFilesValidation;
    private final IfExistsCategoriesValidation ifExistsCategories;
    private final FileSizeValidation fileSizeValidation;

    public ProductController(ProductService service, ProductRepository repository, MultipleFilesValidation validation,
    IfExistsCategoriesValidation ifExistsCategories, FileSizeValidation fileSizeValidation) {
        this.service = service;
        this.repository = repository;
        this.multipleFilesValidation = validation;
        this.ifExistsCategories = ifExistsCategories;
        this.fileSizeValidation = fileSizeValidation;
    }

    @GetMapping
    public Page<Product> viewAll(@PageableDefault Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> view(@PathVariable String id) {
        Optional<Product> productDb = service.findOne(id);

        return productDb.map(product ->
            ResponseEntity.ok().body(product)
        ).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@ModelAttribute @Valid NewProductDTO product) {
        NewProductDTO newProduct = service.save(product);
        return ResponseEntity.ok().body(newProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@ModelAttribute @Valid UpdateProductDTO product, BindingResult result,
    @PathVariable String id, @RequestPart(required = false) List<MultipartFile> images) {
        handleValidations(images, product.categoriesList(), result);
        handleObjectError(product, id, result);

        Optional<UpdateProductDTO> productDb = service.update(id, product, images);

        if (productDb.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(productDb.get());

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

    public ResponseEntity<Map<String, String>> validate(BindingResult result) {
        Map<String, String> errors = new HashMap<>();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });

        result.getGlobalErrors().forEach(err -> {
            errors.put(err.getObjectName(),"El campo " + err.getObjectName() + " " + err.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    public void handleValidations(List<MultipartFile> images, List<String> categories, BindingResult result) {
        multipleFilesValidation.validate(images, result);
        ifExistsCategories.validate(categories, result);

        if (images != null) {
            String key = "productImages";

            images.forEach(productImage -> fileSizeValidation.validate(
                    Arrays.asList(key, productImage), result
            ));
        }
    }

    public void handleObjectError(UpdateProductDTO product, String id, BindingResult result) {
        repository.findById(id).ifPresent(prod -> {
            if (prod.getProductName().equals(product.productName())) return;

            repository.findByProductName(product.productName()).ifPresent(p -> {
                String messageError = "tiene un valor existente";
                result.addError(new ObjectError("productName", messageError));
            });
        });
    }
}
