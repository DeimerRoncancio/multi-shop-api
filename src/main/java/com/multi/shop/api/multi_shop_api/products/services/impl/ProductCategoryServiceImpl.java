package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.CategoryResponseDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.mappers.ProductCategoryMapper;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductCategoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryRepository repository;

    public ProductCategoryServiceImpl(ProductCategoryRepository repository) {
        this.repository = repository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        Page<ProductCategory> categories = repository.findAll(pageable);

        return categories.map(category -> {
            List<ProductItemDTO> items = category.getProducts().stream()
                .map(product -> new ProductItemDTO(
                    product.getId(),
                    product.getProductName(),
                    product.getPrice(),
                    product.getProductImages().get(0))
                ).toList();

            return ProductCategoryMapper.mapper.categoryToResponseDTO(category, items);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryResponseDTO> findOne(String id) {
        Optional<ProductCategory> categoryOptional = repository.findById(id);

        return categoryOptional.map(category -> {
            List<ProductItemDTO> items = category.getProducts().stream()
                .map(product -> new ProductItemDTO(
                    product.getId(),
                    product.getProductName(),
                    product.getPrice(),
                    product.getProductImages().get(0)))
                .toList();

            return ProductCategoryMapper.mapper.categoryToResponseDTO(category, items);
        });
    }

    @Override
    @Transactional
    public ProductCategoryDTO save(ProductCategoryDTO dto) {
        ProductCategory category = ProductCategoryMapper.mapper.categoryDTOtoCategory(dto);
        repository.save(category);
        return dto;
    }

    @Override
    @Transactional
    public Optional<ProductCategoryDTO> update(String id, ProductCategoryDTO dto) {
        return repository.findById(id).map(categoryDb -> {
            ProductCategoryMapper.mapper.toUpdateCategory(dto, categoryDb);
            repository.save(categoryDb);

            return dto;
        });
    }

    @Override
    @Transactional
    public Optional<ProductCategory> delete(String id) {
        Optional<ProductCategory> optionalCategory = repository.findById(id);
        optionalCategory.ifPresent(repository::delete);
        return optionalCategory;
    }

    @Override
    @Transactional
    public List<ProductCategory> findCategoriesByName(List<String> categoryNames) {
        return repository.findByCategoryNameIn(categoryNames);
    }

    @Override
    @Transactional(readOnly = true)
    public Long categoriesSize() {
        return repository.count();
    }
}
