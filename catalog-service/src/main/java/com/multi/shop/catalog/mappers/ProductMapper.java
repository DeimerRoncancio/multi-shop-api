package com.multi.shop.catalog.mappers;

import com.multi.shop.catalog.dtos.ProductDTO;
import com.multi.shop.catalog.dtos.ProductResponseDTO;
import com.multi.shop.catalog.dtos.VariantDTO;
import com.multi.shop.catalog.entities.Variant;
import com.multi.shop.catalog.services.ImageNames;
import com.multi.shop.catalog.media.StoredImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import com.multi.shop.catalog.entities.Product;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageIds", ignore = true)
    @Mapping(target = "variants", expression = "java(variantsList)")
    Product productDTOtoProduct(ProductDTO dto, List<Variant> variantsList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageIds", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "variants", ignore = true)
    void toUpdateProduct(ProductDTO dto, @MappingTarget Product product);

    @Mapping(target = "categoriesList", ignore = true)
    @Mapping(target = "productImages", source = "productImages", qualifiedByName = "sortImages")
    ProductDTO productToProductDTO(Product product, List<StoredImage> productImages);

    @Mapping(target = "categoriesList", ignore = true)
    @Mapping(target = "variants", expression = "java(variants)")
    @Mapping(target = "productImages", source = "productImages", qualifiedByName = "sortImages")
    ProductResponseDTO productToResponseDTO(Product product, List<VariantDTO> variants, List<StoredImage> productImages);

    @Named("sortImages")
    default List<StoredImage> sortImages(List<StoredImage> images) {
        return ImageNames.sorted(images);
    }
}
