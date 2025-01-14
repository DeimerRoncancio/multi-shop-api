package com.multi.shop.api.multi_shop_api.services;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.entities.Image;
import com.multi.shop.api.multi_shop_api.entities.Product;
import com.multi.shop.api.multi_shop_api.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.entities.dtos.NewProductDTO;
import com.multi.shop.api.multi_shop_api.entities.dtos.UpdateProductDTO;
import com.multi.shop.api.multi_shop_api.mappers.ProductMapper;
import com.multi.shop.api.multi_shop_api.repositories.ProductCategoryRepository;
import com.multi.shop.api.multi_shop_api.repositories.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository repository;
    private final ProductCategoryRepository categoryRepository;
    private final ImageService imageService;

    public ProductServiceImpl(ProductRepository repository, ProductCategoryRepository categoryRepository, 
    ImageService imageService) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.imageService = imageService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findOne(String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public NewProductDTO save(NewProductDTO dto, List<MultipartFile> files) {
        List<ProductCategory> categoryList =  categoryRepository.findByCategoryNameIn(dto.getCategoriesList());
        dto.setCategories(categoryList);

        files.forEach(img -> {
            Image image = uploadProductImage(img);
            dto.getProductImages().add(image);
        });

        Product product = ProductMapper.mapper.productCreateDTOtoProduct(dto);
        repository.save(product);
        return dto;
    }

    @Override
    @Transactional
    public Optional<Product> update(String id, UpdateProductDTO dto, List<MultipartFile> files) {
        Optional<Product> productOptional = repository.findById(id);
        
        productOptional.ifPresent(productDb -> {
            List<ProductCategory> categoriesDb = categoryRepository.findByCategoryNameIn(dto.getCategoriesList());
            List<ProductCategory> productCategories = updateProductCategories(productDb.getCategories(), categoriesDb);
            List<Image> productImages = updateProductImages(productDb.getProductImages(), files);

            dto.setCategories(productCategories);
            dto.setImages(productImages);
            ProductMapper.mapper.toUpdateProduct(dto, productDb);

            repository.save(productDb);
        });

        return productOptional;
    }

    @Override
    @Transactional
    public Optional<Product> delete(String id) {
        Optional<Product> productOptional = repository.findById(id);

        productOptional.ifPresent(product -> {
            if (!product.getProductImages().isEmpty()) {
                product.getProductImages().forEach(img -> {
                    deleteProductImage(img);
                });
            }

            repository.delete(productOptional.get());
        });

        return productOptional;
    }

    public List<Image> updateProductImages(List<Image> productImages, List<MultipartFile> files) {
        List<Image> imagesToRemove = productImages.stream()
            .filter(img -> files.stream()
            .noneMatch(file -> Optional.ofNullable(file.getOriginalFilename())
            .orElse("").equals(img.getName())))
            .collect(Collectors.toList());

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
            .collect(Collectors.toList());
        
        productCategories.removeAll(categoriesToRemove);

        categories.stream()
            .filter(cats -> productCategories.stream()
            .noneMatch(cat -> cat.getCategoryName().equals(cats.getCategoryName())))
            .forEach(c -> productCategories.add(c));
        
        return productCategories;
    }
    
    public void deleteProductImage(Image image) {
        try {
            imageService.deleteImage(image);
        } catch(IOException e) {
            logger.error("Exception to try delete image: " + e);
        }
    }

    public Image uploadProductImage(MultipartFile file) {
        Image image = null;

        if (file != null && !file.isEmpty()) {
            try {
                image = imageService.uploadImage(file);
            } catch (IOException e) {
                logger.error("Exception to try add the image: " + e);
            }
        }

        return image;
    }
}
