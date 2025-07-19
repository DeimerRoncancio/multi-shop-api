package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;
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
    public List<ProductCategory> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductCategory> findOne(String id) {
        return repository.findById(id);
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
        Optional<ProductCategory> optionalCategory = repository.findById(id);

        if (optionalCategory.isEmpty()) return Optional.empty();

        ProductCategory categoryDb = optionalCategory.get();
        ProductCategoryMapper.mapper.toUpdateCategory(dto, categoryDb);
        repository.save(categoryDb);

        return Optional.of(dto);
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
    public int categoriesSize() {
        return findAll().size();
    }
}
