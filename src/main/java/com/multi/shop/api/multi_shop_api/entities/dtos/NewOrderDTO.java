package com.multi.shop.api.multi_shop_api.entities.dtos;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.entities.Order;
import com.multi.shop.api.multi_shop_api.entities.User;
import com.multi.shop.api.multi_shop_api.validation.IfExists;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NewOrderDTO {

    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExists(entity = Order.class, field = "orderName", message = "{IfExists.order.name}")
    private String orderName;
    private String notes;

    @NotNull(message = "{NotBlank.validation.text}")
    private Date orderDate;

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
}
