package com.multi.shop.api.multi_shop_api.products.controllers;

import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import jakarta.validation.Valid;

import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.entities.dtos.NewProductDTO;
import com.multi.shop.api.multi_shop_api.products.entities.dtos.UpdateProductDTO;
import com.multi.shop.api.multi_shop_api.products.services.ProductService;
import com.multi.shop.api.multi_shop_api.common.validation.FileSizeValidation;
import com.multi.shop.api.multi_shop_api.products.validation.IfExistsCategoriesValidation;
import com.multi.shop.api.multi_shop_api.products.validation.MultipleFilesValidation;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public List<Product> viewAll() {
        return service.findAll();
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
    public ResponseEntity<?> create(@Valid @ModelAttribute NewProductDTO product, BindingResult result, 
    @RequestPart(required = false) List<MultipartFile> images) {
        handleValidations(images, product.getCategoriesList(), result);

        if (result.hasErrors())
            return validate(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(product, images));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@Valid @ModelAttribute UpdateProductDTO product, BindingResult result, 
    @PathVariable String id, @RequestPart(required = false) List<MultipartFile> images) {
        handleValidations(images, product.getCategoriesList(), result);
        handleObjectError(product, id, result);

        if (result.hasErrors())
            return validate(result);

        Optional<Product> productDb = service.update(id, product, images);

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
            if (prod.getProductName().equals(product.getProductName())) return;

            repository.findByProductName(product.getProductName()).ifPresent(p -> {
                String messageError = "tiene un valor existente";
                result.addError(new ObjectError("productName", messageError));
            });
        });
    }
}
