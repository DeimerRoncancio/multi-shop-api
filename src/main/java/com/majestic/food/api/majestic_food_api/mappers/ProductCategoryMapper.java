package com.majestic.food.api.majestic_food_api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.entities.dtos.NewProductCategoryDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.UpdateProductCategoryDTO;

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
