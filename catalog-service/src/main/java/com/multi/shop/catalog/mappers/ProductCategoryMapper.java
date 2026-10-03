package com.multi.shop.catalog.mappers;

import com.multi.shop.catalog.dtos.ProductCategoryDTO;
import com.multi.shop.catalog.dtos.CategoryResponseDTO;
import com.multi.shop.catalog.dtos.ProductItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.multi.shop.catalog.entities.ProductCategory;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {
    @Mapping(target = "id", ignore = true)
    ProductCategory categoryDTOtoCategory(ProductCategoryDTO dto);

    ProductCategoryDTO categoryDTOtoCategory(ProductCategory category);

    @Mapping(target = "products", expression = "java(productsItem)")
    CategoryResponseDTO categoryToResponseDTO(ProductCategory category, List<ProductItemDTO> productsItem);

    @Mapping(target = "id", ignore = true)
    void toUpdateCategory(ProductCategoryDTO dto, @MappingTarget ProductCategory category);
}
