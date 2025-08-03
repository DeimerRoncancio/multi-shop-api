package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.images.services.ImageService;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductResponseDTO;
import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;
import com.multi.shop.api.multi_shop_api.products.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.mappers.ProductMapper;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository repository;
    private final ProductCategoryService categoryService;
    private final ImageService imageService;

    public ProductServiceImpl(ProductRepository repository, ProductCategoryService categoryService, 
    ImageService imageService) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.imageService = imageService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        Page<Product> products = repository.findAll(pageable);
        return products.map(ProductMapper.MAPPER::productToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponseDTO> findOne(String id) {
        Optional<Product> product = repository.findById(id);
        return product.map(ProductMapper.MAPPER::productToResponseDTO);
    }

    @Override
    @Transactional
    public ProductDTO save(ProductDTO dto) {
        Product product = ProductMapper.MAPPER.productDTOtoProduct(dto);

        List<ProductCategory> categoryList =  categoryService.findCategoriesByName(dto.categoriesList());
        product.setCategories(categoryList);

        dto.images().forEach(img -> {
            Image image = uploadProductImage(img);
            product.getProductImages().add(image);
        });

        repository.save(product);
        return ProductMapper.MAPPER.productToProductDTO(product);
    }

    @Override
    @Transactional
    public Optional<ProductDTO> update(String id, ProductDTO dto) {
        return repository.findById(id).map(productDb -> {
            List<ProductCategory> categoriesDb = categoryService.findCategoriesByName(dto.categoriesList());
            List<ProductCategory> productCategories = updateProductCategories(
                productDb.getCategories(), categoriesDb
            );

            List<Image> productImages = updateProductImages(
                productDb.getProductImages(), dto.images(), dto.imagesToRemove()
            );

            productDb.setCategories(productCategories);
            productDb.setProductImages(productImages);
            ProductMapper.MAPPER.toUpdateProduct(dto, productDb);

            repository.save(productDb);

            return ProductMapper.MAPPER.productToProductDTO(productDb);
        });
    }

    @Override
    @Transactional
    public Optional<Product> delete(String id) {
        return repository.findById(id).map(product -> {
            if (!product.getProductImages().isEmpty())
                product.getProductImages().forEach(this::deleteProductImage);

            repository.delete(product);
            return product;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Long productsSize() {
        return repository.count();
    }

    public List<Image> updateProductImages(List<Image> productImages, List<MultipartFile> files,
    List<String> removeImagesId) {
        if (removeImagesId != null && !removeImagesId.isEmpty()){
            Set<String> idsToRemove = new HashSet<>(removeImagesId);
            List<Image> imagesToRemove = productImages.stream()
                .filter(img -> idsToRemove.contains(img.getImageId()))
                .toList();

            productImages.removeAll(imagesToRemove);
            imagesToRemove.forEach(this::deleteProductImage);
        }

        if (files != null) {
            Set<String> imageNames = productImages.stream()
                .map(Image::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

            files.stream()
                .filter(file -> Optional.ofNullable(file.getOriginalFilename())
                    .map(String::toLowerCase)
                    .map(name -> !imageNames.contains(name))
                    .orElse(false))
                .map(this::uploadProductImage)
                .forEach(productImages::add);
        }

        return productImages;
    }

    public List<ProductCategory> updateProductCategories(List<ProductCategory> productCategories, 
    List<ProductCategory> categories) {
        productCategories.removeIf(cat -> !categories.contains(cat));

        categories.stream()
            .filter(cats -> !productCategories.contains(cats))
            .forEach(productCategories::add);

        return productCategories;
    }
    
    public void deleteProductImage(Image image) {
        try {
            imageService.deleteImage(image);
        } catch(IOException e) {
            logger.warn("Exception to try delete image: {}", String.valueOf(e));
        }
    }

    public Image uploadProductImage(MultipartFile file) {
        Image image = null;

        if (file != null && !file.isEmpty()) {
            try {
                image = imageService.uploadImage(file);
            } catch (IOException e) {
                logger.error("Exception trying add image: {}", String.valueOf(e));
            }
        }

        return image;
    }
}
