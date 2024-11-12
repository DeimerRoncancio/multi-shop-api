package com.majestic.food.api.majestic_food_api.entities.dtos;

import java.math.BigDecimal;
import java.util.List;

import com.majestic.food.api.majestic_food_api.entities.Product;
import com.majestic.food.api.majestic_food_api.entities.ProductCategory;
import com.majestic.food.api.majestic_food_api.validation.IfExistsUpdate;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductUpdateDTO {

    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExistsUpdate(entity = Product.class, field = "productName", message = "{IfExists.product.name}")
    private String productName;

    @NotBlank(message = "{NotBlank.validation.text}")
    @Size(max = 140, message = "{Size.product.description}")
    private String description;
    
    @NotNull(message = "{NotBlank.validation.text}")
    private BigDecimal price;

    @ManyToMany
    @JoinTable(
        name = "categories_to_products",
        joinColumns = @JoinColumn(name = "id_product"),
        inverseJoinColumns = @JoinColumn(name = "id_category"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_product", "id_category"}))
    private List<ProductCategory> categories;

    public ProductUpdateDTO() {
    }

    public ProductUpdateDTO(String productName, String description, BigDecimal price, List<ProductCategory> categories) {
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
}
