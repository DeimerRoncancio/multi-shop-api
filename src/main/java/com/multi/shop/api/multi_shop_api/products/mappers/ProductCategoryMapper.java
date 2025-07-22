package com.multi.shop.api.multi_shop_api.products.mappers;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;

@Mapper
public interface ProductCategoryMapper {
    ProductCategoryMapper mapper = Mappers.getMapper(ProductCategoryMapper.class);

    @Mapping(target = "id", ignore = true)
    ProductCategory categoryDTOtoCategory(ProductCategoryDTO dto);

    CategoryResponseDTO categoryToResponseDTO(ProductCategory category);

    @Mapping(target = "id", ignore = true)
    void toUpdateCategory(ProductCategoryDTO dto, @MappingTarget ProductCategory category);
}
