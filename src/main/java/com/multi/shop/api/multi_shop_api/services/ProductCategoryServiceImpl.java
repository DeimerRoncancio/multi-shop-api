package com.multi.shop.api.multi_shop_api.services;

import org.springframework.transaction.annotation.Transactional;

import com.multi.shop.api.multi_shop_api.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.entities.dtos.NewProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.entities.dtos.UpdateProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.mappers.ProductCategoryMapper;
import com.multi.shop.api.multi_shop_api.repositories.ProductCategoryRepository;

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
    public NewProductCategoryDTO save(NewProductCategoryDTO dto) {
        ProductCategory category = ProductCategoryMapper.mapper.categoryCreateDTOtoCategory(dto);
        
        repository.save(category);
        return dto;
    }

    @Override
    @Transactional
    public Optional<ProductCategory> update(String id, UpdateProductCategoryDTO dto) {
        Optional<ProductCategory> optionalCategory = repository.findById(id);
        
        optionalCategory.ifPresent(categoryDb -> {
            ProductCategoryMapper.mapper.toUpdateCategory(dto, categoryDb);

            repository.save(categoryDb);
        });

        return optionalCategory;
    }

    @Override
    @Transactional
    public Optional<ProductCategory> delete(String id) {
        Optional<ProductCategory> optionalCategory = repository.findById(id);

        if (optionalCategory.isPresent())
            repository.delete(optionalCategory.get());
        
        return optionalCategory;
    }

    @Override
    @Transactional
    public List<ProductCategory> findCategoriesByName(List<String> categoryNames) {
        return repository.findByCategoryNameIn(categoryNames);
    }
}
