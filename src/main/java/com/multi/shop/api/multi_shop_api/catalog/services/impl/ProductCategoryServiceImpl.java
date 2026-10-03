package com.multi.shop.api.multi_shop_api.catalog.services.impl;

import com.multi.shop.api.multi_shop_api.catalog.dtos.ProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.catalog.dtos.CategoryResponseDTO;
import com.multi.shop.api.multi_shop_api.catalog.services.ImageNames;
import com.multi.shop.api.multi_shop_api.media.api.MediaApi;
import com.multi.shop.api.multi_shop_api.media.api.StoredImage;
import com.multi.shop.api.multi_shop_api.catalog.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.catalog.services.ProductCategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.multi.shop.api.multi_shop_api.catalog.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.catalog.mappers.ProductCategoryMapper;
import com.multi.shop.api.multi_shop_api.catalog.repositories.ProductCategoryRepository;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryRepository repository;
    private final ProductCategoryMapper categoryMapper;
    private final MediaApi mediaApi;

    public ProductCategoryServiceImpl(ProductCategoryRepository repository, ProductCategoryMapper categoryMapper, MediaApi mediaApi) {
        this.repository = repository;
        this.categoryMapper = categoryMapper;
        this.mediaApi = mediaApi;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        Page<ProductCategory> categories = repository.findAll(pageable);
        Map<String, StoredImage> images = imagesOf(categories.getContent());

        return categories.map(category -> toResponseDTO(category, images));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryResponseDTO> findOne(String id) {
        Optional<ProductCategory> categoryOptional = repository.findById(id);

        return categoryOptional.map(category -> toResponseDTO(category, imagesOf(List.of(category))));
    }

    private CategoryResponseDTO toResponseDTO(ProductCategory category, Map<String, StoredImage> images) {
        List<ProductItemDTO> items = category.getProducts().stream()
            .map(product -> new ProductItemDTO(
                product.getId(),
                product.getProductName(),
                product.getPrice(),
                ImageNames.main(product.getImageIds().stream()
                    .map(images::get)
                    .filter(Objects::nonNull)
                    .toList())))
            .toList();

        return categoryMapper.categoryToResponseDTO(category, items);
    }

    private Map<String, StoredImage> imagesOf(List<ProductCategory> categories) {
        List<String> ids = categories.stream()
            .flatMap(category -> category.getProducts().stream())
            .flatMap(product -> product.getImageIds().stream())
            .distinct()
            .toList();

        return mediaApi.findAll(ids).stream()
            .collect(Collectors.toMap(StoredImage::id, Function.identity(), (first, second) -> first));
    }

    @Override
    @Transactional
    public ProductCategoryDTO save(ProductCategoryDTO dto) {
        ProductCategory category = categoryMapper.categoryDTOtoCategory(dto);
        repository.save(category);
        return dto;
    }

    @Override
    @Transactional
    public Optional<ProductCategoryDTO> update(String id, ProductCategoryDTO dto) {
        return repository.findById(id).map(categoryDb -> {
            categoryMapper.toUpdateCategory(dto, categoryDb);
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

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> categoriesStats(){
        Map<String, Long> categoriesStats = new HashMap<>();

        categoriesStats.put("totalCategories", categoriesSize());
        categoriesStats.put("obsoleteCategories", repository.countObsoleteCategories());

        return categoriesStats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> latestCategories(){
        return repository.findTop3ByOrderByCreatedAtDesc().stream()
                .map(categoryMapper::categoryDTOtoCategory).toList();
    }
}
