package com.majestic.food.api.majestic_food_api.entities;

import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import com.majestic.food.api.majestic_food_api.validation.IfExists;

import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Column;

@Entity
@Table(name = "prod_categories")
public class ProductCategory {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(unique = true)
    @IfExists(entity = ProductCategory.class, field = "categoryName", message = "{IfExists.category.name}")
    @NotBlank(message = "{NotBlank.validation.text}")
    private String categoryName;

    @ManyToMany(mappedBy = "categories")
    private List<Product> products;

    public ProductCategory() {
    }

    public ProductCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
