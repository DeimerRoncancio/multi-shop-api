package com.majestic.food.api.majestic_food_api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.majestic.food.api.majestic_food_api.entities.Product;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductUpdateDTO;

@Mapper
public interface ProductMapper {

    ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "id", ignore = true)
    Product productCreateDTOtoProduct(ProductCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    void toUpdateProduct(ProductUpdateDTO dto, @MappingTarget Product product);
}
