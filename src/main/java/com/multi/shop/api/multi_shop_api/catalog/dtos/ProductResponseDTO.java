package com.multi.shop.api.multi_shop_api.catalog.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import com.multi.shop.api.multi_shop_api.common.validation.ImageFormat;
import com.multi.shop.api.multi_shop_api.common.validation.NotEmptyFile;
import com.multi.shop.api.multi_shop_api.media.api.StoredImage;
import com.multi.shop.api.multi_shop_api.catalog.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.catalog.entities.Variant;
import com.multi.shop.api.multi_shop_api.catalog.validation.ExistingCategories;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Transient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ProductResponseDTO(
    String id,
    String productName,
    String description,
    Long price,

    @JsonIgnoreProperties("id")
    List<StoredImage> productImages,

    @JsonIgnoreProperties({"id", "products"})
    List<ProductCategory> categories,

    List<VariantDTO> variants,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    List<MultipartFile> images,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    List<String> categoriesList
) {
}
