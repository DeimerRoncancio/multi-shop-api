package com.multi.shop.api.multi_shop_api.products.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.dtos.NewProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.UpdateProductCategoryDTO;

@Mapper
public interface ProductCategoryMapper {
    ProductCategoryMapper mapper = Mappers.getMapper(ProductCategoryMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    ProductCategory categoryCreateDTOtoCategory(NewProductCategoryDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void toUpdateCategory(UpdateProductCategoryDTO dto, @MappingTarget ProductCategory category);
}
