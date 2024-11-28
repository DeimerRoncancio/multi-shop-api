package com.majestic.food.api.majestic_food_api.entities.dtos;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.majestic.food.api.majestic_food_api.entities.Order;
import com.majestic.food.api.majestic_food_api.entities.Product;
import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.validation.IfExistsUpdate;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderDTO {

    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExistsUpdate(entity = Order.class, field = "orderName", message = "{IfExists.order.name}")
    private String orderName;
    private String notes;

    @NotNull(message = "{NotBlank.validation.text}")
    private Date orderDate;

    @ManyToMany
    @JoinTable(
        name = "products_to_orders",
        joinColumns = @JoinColumn(name = "id_order"),
        inverseJoinColumns = @JoinColumn(name = "id_product"))
    @JsonIgnoreProperties({"id", "categories"})
    private List<Product> product;

    @ManyToOne
    @JoinColumn(name = "id_user")
    @JsonIgnoreProperties({"id", "orders", "roles"})
    private User user;
    
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
