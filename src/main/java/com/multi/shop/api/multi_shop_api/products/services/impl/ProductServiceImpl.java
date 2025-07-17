package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.images.services.ImageService;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;
import com.multi.shop.api.multi_shop_api.products.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Optional;

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
    public Page<Product> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findOne(String id) {
        return repository.findById(id);
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
        Optional<Product> productOptional = repository.findById(id);

        if (productOptional.isPresent()) {
            Product productDb = productOptional.get();

            List<ProductCategory> categoriesDb = categoryService.findCategoriesByName(dto.categoriesList());
            List<ProductCategory> productCategories = updateProductCategories(
                productDb.getCategories(), categoriesDb
            );
            List<Image> productImages = updateProductImages(productDb.getProductImages(), dto.images());

            productDb.setCategories(productCategories);
            productDb.setProductImages(productImages);
            ProductMapper.MAPPER.toUpdateProduct(dto, productDb);

            repository.save(productDb);
            return Optional.of(ProductMapper.MAPPER.productToProductDTO(productDb));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<Product> delete(String id) {
        Optional<Product> productOptional = repository.findById(id);

        productOptional.ifPresent(product -> {
            if (!product.getProductImages().isEmpty()) {
                product.getProductImages().forEach(this::deleteProductImage);
            }

            repository.delete(productOptional.get());
        });

        return productOptional;
    }

    @Override
    @Transactional(readOnly = true)
    public int productsSize() {
        return findAll(Pageable.unpaged()).getContent().size();
    }

    public List<Image> updateProductImages(List<Image> productImages, List<MultipartFile> files) {
        List<Image> imagesToRemove = productImages.stream()
            .filter(img -> files.stream()
            .noneMatch(file -> Optional.ofNullable(file.getOriginalFilename())
            .orElse("").equals(img.getName())))
            .toList();

        productImages.removeAll(imagesToRemove);
        imagesToRemove.forEach(this::deleteProductImage);

        files.stream()
            .filter(file -> productImages.stream()
            .noneMatch(img -> img.getName()
            .equals(Optional.ofNullable(file.getOriginalFilename()).orElse(""))))
            .forEach(file -> productImages.add(uploadProductImage(file)));
        
        return productImages;
    }

    public List<ProductCategory> updateProductCategories(List<ProductCategory> productCategories, 
    List<ProductCategory> categories) {
        List<ProductCategory> categoriesToRemove = productCategories.stream()
            .filter(cat -> categories.stream()
            .noneMatch(cats -> cats.getCategoryName()
            .equals(cat.getCategoryName())))
            .toList();
        
        productCategories.removeAll(categoriesToRemove);

        categories.stream()
            .filter(cats -> productCategories.stream()
            .noneMatch(cat -> cat.getCategoryName().equals(cats.getCategoryName())))
            .forEach(productCategories::add);
        
        return productCategories;
    }
    
    public void deleteProductImage(Image image) {
        try {
            imageService.deleteImage(image);
        } catch(IOException e) {
            logger.error("Exception to try delete image: {}", String.valueOf(e));
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
