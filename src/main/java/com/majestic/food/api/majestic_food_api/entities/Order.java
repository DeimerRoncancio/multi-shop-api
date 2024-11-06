package com.majestic.food.api.majestic_food_api.entities;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Column;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @NotBlank
    private String orderName;
    private String notes;

    @NotBlank
    private Date orderDate;

    @ManyToMany
    @JoinTable(
        name = "products_to_orders",
        joinColumns = @JoinColumn(name = "id_order"),
        inverseJoinColumns = @JoinColumn(name = "id_product"))
    private List<Product> product;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    public Order() {
    }

    public Order(String orderName) {
        this.orderName = orderName;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getOrderName() {
        return orderName;
    }
    
    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Date getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    public List<Product> getProduct() {
        return product;
    }
    
    public void setProduct(List<Product> product) {
        this.product = product;
    }
}
