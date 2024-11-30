package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Image;
import com.majestic.food.api.majestic_food_api.entities.Product;
import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.entities.dtos.NewProductDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.UpdateProductDTO;
import com.majestic.food.api.majestic_food_api.mappers.ProductMapper;
import com.majestic.food.api.majestic_food_api.repositories.ProductCategoryRepository;
import com.majestic.food.api.majestic_food_api.repositories.ProductRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
    public Product save(NewProductDTO dto, List<MultipartFile> files) {
        List<ProductCategory> categoryList =  categoryRepository.findByCategoryNameIn(dto.getCategoriesList());
        dto.setCategories(categoryList);

        files.forEach(img -> {
            Image image = uploadProductImage(img);
            dto.getImages().add(image);
        });

        Product product = ProductMapper.mapper.productCreateDTOtoProduct(dto);

        return repository.save(product);
    }

    @Override
    @Transactional
    public Optional<Product> update(String id, UpdateProductDTO dto, List<MultipartFile> files) {
        Optional<Product> productOptional = repository.findById(id);
        
        productOptional.ifPresent(productDb -> {
            List<ProductCategory> categories = categoryRepository.findByCategoryNameIn(dto.getCategoriesList());
            List<Image> productImages = updateProductImages(productDb.getImages(), files);
            
            dto.setCategories(categories);
            ProductMapper.mapper.toUpdateProduct(dto, productDb);
            productDb.setImages(productImages);

            repository.save(productDb);
        });

        return productOptional;
    }

    @Override
    @Transactional
    public Optional<Product> delete(String id) {
        Optional<Product> productOptional = repository.findById(id);

        productOptional.ifPresent(product -> {
            if (!product.getImages().isEmpty()) {
                product.getImages().forEach(img -> {
                    deleteProductImage(img);
                });
            }

            repository.delete(productOptional.get());
        });

        return productOptional;
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

    public List<Image> updateProductImages(List<Image> productImages, List<MultipartFile> files) {
        List<Image> imagesToRemove = productImages.stream().filter(img -> files.stream()
            .noneMatch(file -> Optional.ofNullable(file.getOriginalFilename()).orElse("")
            .equals(img.getName()))).collect(Collectors.toList());
        
        productImages.removeAll(imagesToRemove);
        imagesToRemove.forEach(this::deleteProductImage);
        
        if (!files.isEmpty())
            files.stream().filter(file -> productImages.stream()
                .noneMatch(img -> img.getName().equals(file.getOriginalFilename())))
                .forEach(file -> productImages.add(uploadProductImage(file)));
        
        return productImages;
    }

    public void deleteProductImage(Image image) {
        try {
            imageService.deleteImage(image);
        } catch(IOException e) {
            logger.error("Exception to try delete image: " + e);
        }
    }
}
