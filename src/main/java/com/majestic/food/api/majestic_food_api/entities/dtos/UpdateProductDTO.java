package com.majestic.food.api.majestic_food_api.entities.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.majestic.food.api.majestic_food_api.entities.Image;
import com.majestic.food.api.majestic_food_api.entities.Product;
import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.validation.IfExistsUpdate;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateProductDTO {

    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExistsUpdate(entity = Product.class, field = "productName", message = "{IfExists.product.name}")
    private String productName;

    @NotBlank(message = "{NotBlank.validation.text}")
    @Size(max = 140, message = "{Size.product.description}")
    private String description;
    
    @NotNull(message = "{NotBlank.validation.text}")
    private BigDecimal price;

    private List<Image> images;

    private List<ProductCategory> categories;

    @NotEmpty
    @Transient
    private List<String> categoriesList;

    public UpdateProductDTO() {
        categories = new ArrayList<>();
        images = new ArrayList<> ();
    }

    public UpdateProductDTO(String productName, String description, BigDecimal price, List<ProductCategory> categories) {
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

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<String> getCategoriesList() {
        return categoriesList;
    }

    public void setCategoriesList(List<String> categoryList) {
        this.categoriesList = categoryList;
    }
}
