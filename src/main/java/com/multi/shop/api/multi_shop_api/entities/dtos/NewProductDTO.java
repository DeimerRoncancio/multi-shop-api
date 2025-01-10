package com.multi.shop.api.multi_shop_api.entities.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.entities.Image;
import com.multi.shop.api.multi_shop_api.entities.Product;
import com.multi.shop.api.multi_shop_api.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.validation.IfExists;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NewProductDTO {

    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExists(entity = Product.class, field = "productName", message = "{IfExists.product.name}")
    private String productName;

    @NotBlank(message = "{NotBlank.validation.text}")
    @Size(max = 140, message = "{Size.product.description}")
    private String description;
    
    @NotNull(message = "{NotBlank.validation.text}")
    private BigDecimal price;

    @JsonIgnoreProperties("id")
    private List<Image> images;
    
    @JsonIgnoreProperties({"id", "products"})
    private List<ProductCategory> categories;
    
    @Transient
    @NotEmpty(message = "{NotEmpty.validation.list}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> categoriesList;

    public NewProductDTO() {
        categoriesList = new ArrayList<>();
        images = new ArrayList<> ();
    }

    public NewProductDTO(String productName, String description, BigDecimal price, List<ProductCategory> categories) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.categories = categories;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<ProductCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<ProductCategory> productCategory) {
        this.categories = productCategory;
    }

    public List<String> getCategoriesList() {
        return categoriesList;
    }

    public void setCategoriesList(List<String> categoriesList) {
        this.categoriesList = categoriesList;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
}
