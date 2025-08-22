package com.multi.shop.api.multi_shop_api.products.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Entity
@Table(name = "variants")
public class Variant {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private String id;
    @Column(unique = true)
    private String name;
    private String tag;
    @Column(name = "variant_values")
    private String values;

    @ManyToMany(mappedBy = "variants")
    private List<Product> products;

    public Variant() {
    }

    public Variant(String id, String name, String values, String tag) {
        this.id = id;
        this.name = name;
        this.values = values;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
