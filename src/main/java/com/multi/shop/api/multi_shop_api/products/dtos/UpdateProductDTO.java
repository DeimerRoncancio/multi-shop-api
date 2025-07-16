package com.multi.shop.api.multi_shop_api.products.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;

import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import com.multi.shop.api.multi_shop_api.products.validation.ExistingCategories;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExists(entity = "Product", field = "productName")
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
    @ExistingCategories
    @NotEmpty(message = "{NotEmpty.validation.list}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    List<String> categoriesList
) {
}
