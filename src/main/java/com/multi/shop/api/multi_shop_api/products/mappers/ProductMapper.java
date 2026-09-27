package com.multi.shop.api.multi_shop_api.products.mappers;

import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductResponseDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.images.services.ImageNames;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", expression = "java(variantsList)")
    Product productDTOtoProduct(ProductDTO dto, List<Variant> variantsList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "variants", ignore = true)
    void toUpdateProduct(ProductDTO dto, @MappingTarget Product product);

    @Mapping(target = "categoriesList", ignore = true)
    @Mapping(target = "productImages", source = "productImages", qualifiedByName = "sortImages")
    ProductDTO productToProductDTO(Product product);

    @Mapping(target = "categoriesList", ignore = true)
    @Mapping(target = "variants", expression = "java(variants)")
    @Mapping(target = "productImages", source = "product.productImages", qualifiedByName = "sortImages")
    ProductResponseDTO productToResponseDTO(Product product, List<VariantDTO> variants);

    @Named("sortImages")
    default List<Image> sortImages(List<Image> images) {
        return ImageNames.sorted(images);
    }
}
