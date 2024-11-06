package com.majestic.food.api.majestic_food_api.entities;

import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "prod_types")
public class ProductType {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(unique = true)
    private String typeName;

    @ManyToMany(mappedBy = "productTypes")
    private List<Product> products;

    public ProductType() {
    }

    public ProductType(String typeName) {
        this.typeName = typeName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
