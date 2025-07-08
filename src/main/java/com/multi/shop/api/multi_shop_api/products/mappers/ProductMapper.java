package com.multi.shop.api.multi_shop_api.products.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.dtos.NewProductDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.UpdateProductDTO;

@Mapper
public interface ProductMapper {
    ProductMapper MAPPER = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "id", ignore = true)
    Product productCreateDTOtoProduct(NewProductDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void toUpdateProduct(UpdateProductDTO dto, @MappingTarget Product product);
}
