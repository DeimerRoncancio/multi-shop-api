package com.multi.shop.api.multi_shop_api.products.mappers;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductCategoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;

@Mapper
public interface ProductCategoryMapper {
    ProductCategoryMapper mapper = Mappers.getMapper(ProductCategoryMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    ProductCategory categoryDTOtoCategory(ProductCategoryDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void toUpdateCategory(ProductCategoryDTO dto, @MappingTarget ProductCategory category);
}
