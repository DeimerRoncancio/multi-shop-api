package com.multi.shop.api.multi_shop_api.products.dtos;

import java.util.List;

import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import com.multi.shop.api.multi_shop_api.common.validation.ImageFormat;
import com.multi.shop.api.multi_shop_api.common.validation.NotEmptyFile;
import com.multi.shop.api.multi_shop_api.products.validation.ExistingCategories;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record NewProductDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExists(message = "{IfExists.validation}", field = "productName", entity = "Product")
    String productName,

    @NotBlank(message = "{NotBlank.validation.text}")
    @Size(max = 140, message = "{Size.product.description}")
    String description,

    @NotNull(message = "{NotBlank.validation.text}")
    Long price,

    @JsonIgnoreProperties("id")
    List<Image> productImages,

    @JsonIgnoreProperties({"id", "products"})
    List<ProductCategory> categories,

    @Transient
    @NotEmptyFile
    @ImageFormat(maxSize = 3 * 1024 * 1024)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    List<MultipartFile> images,

    @Transient
    @ExistingCategories
    @NotEmpty(message = "{NotEmpty.validation.list}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    List<String> categoriesList
) {
}
