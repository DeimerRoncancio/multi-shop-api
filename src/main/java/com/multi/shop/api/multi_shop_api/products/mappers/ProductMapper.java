package com.multi.shop.api.multi_shop_api.products.mappers;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.multi.shop.api.multi_shop_api.products.entities.Product;

@Mapper
public interface ProductMapper {
    ProductMapper MAPPER = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "id", ignore = true)
    Product productDTOtoProduct(ProductDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "variants", ignore = true)
    void toUpdateProduct(ProductDTO dto, @MappingTarget Product product);

    @Mapping(target = "categoriesList", ignore = true)
    ProductDTO productToProductDTO(Product product);

    @Mapping(target = "categoriesList", ignore = true)
    ProductResponseDTO productToResponseDTO(Product product);
}
