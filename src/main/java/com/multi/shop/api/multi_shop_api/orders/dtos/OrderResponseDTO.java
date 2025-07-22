package com.multi.shop.api.multi_shop_api.orders.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;

public record OrderResponseDTO(
    String id,
    String orderName,
    String notes,
    Date orderDate,

    @JsonIgnoreProperties({"id", "categories"})
    List<Product> product,

    @JsonIgnoreProperties({"id", "orders", "roles"})
    User user
) {
}
