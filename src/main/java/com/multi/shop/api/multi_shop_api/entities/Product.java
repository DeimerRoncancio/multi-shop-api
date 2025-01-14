package com.multi.shop.api.multi_shop_api.entities;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(unique = true)
    private String productName;
    private String description;
    private BigDecimal price;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "images_to_products",
        joinColumns = @JoinColumn(name = "id_product"),
        inverseJoinColumns = @JoinColumn(name = "id_image"))
    @JsonIgnoreProperties("id")
    private List<Image> productImages;
    
    @ManyToMany
    @JoinTable(
        name = "categories_to_products",
        joinColumns = @JoinColumn(name = "id_product"),
        inverseJoinColumns = @JoinColumn(name = "id_category"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_product", "id_category"}))
    @JsonIgnoreProperties({"id", "products"})
    private List<ProductCategory> categories;

    public Product() {
    }

    public Product(String productName, String description, BigDecimal price, List<ProductCategory> categories) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.categories = categories;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<Image> getProductImages() {
        return productImages;
    }

    public void setProductImages(List<Image> images) {
        this.productImages = images;
    }
}
