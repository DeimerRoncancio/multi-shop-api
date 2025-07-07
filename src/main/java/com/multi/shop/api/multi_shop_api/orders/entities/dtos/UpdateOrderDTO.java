package com.multi.shop.api.multi_shop_api.orders.entities.dtos;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.users.entities.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    String orderName,
    String notes,

    @NotNull(message = "{NotBlank.validation.text}")
    Date orderDate,

    @JsonIgnoreProperties({"id", "categories"})
    List<Product> product,

    @JsonIgnoreProperties({"id", "orders", "roles"})
    User user
) {
}
