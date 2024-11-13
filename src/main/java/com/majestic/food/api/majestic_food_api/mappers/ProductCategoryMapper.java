package com.majestic.food.api.majestic_food_api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductCategoryCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductCategoryUpdateDTO;

@Mapper
public interface ProductCategoryMapper {

    ProductCategoryMapper mapper = Mappers.getMapper(ProductCategoryMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    ProductCategory categoryCreateDTOtoCategory(ProductCategoryCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void toUpdateCategory(ProductCategoryUpdateDTO dto, @MappingTarget ProductCategory category);
}
