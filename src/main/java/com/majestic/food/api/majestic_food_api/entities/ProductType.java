package com.majestic.food.api.majestic_food_api.entities;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "prod_types")
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(unique = true)
    private String typeName;

    @ManyToMany(mappedBy = "prod_types")
    private List<Product> products;

    public ProductType() {
    }

    public ProductType(String typeName) {
        this.typeName = typeName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
