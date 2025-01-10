package com.multi.shop.api.multi_shop_api.entities.dtos;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.entities.Order;
import com.multi.shop.api.multi_shop_api.entities.Product;
import com.multi.shop.api.multi_shop_api.entities.User;
import com.multi.shop.api.multi_shop_api.validation.IfExistsUpdate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderDTO {

    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExistsUpdate(entity = Order.class, field = "orderName", message = "{IfExists.order.name}")
    private String orderName;
    private String notes;

    @NotNull(message = "{NotBlank.validation.text}")
    private Date orderDate;

    @JsonIgnoreProperties({"id", "categories"})
    private List<Product> product;

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
