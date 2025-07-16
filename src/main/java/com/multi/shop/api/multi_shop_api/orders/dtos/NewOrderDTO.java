package com.multi.shop.api.multi_shop_api.orders.dtos;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.common.validation.IfExistsValid;
import com.multi.shop.api.multi_shop_api.orders.entities.Order;
import com.multi.shop.api.multi_shop_api.users.entities.User;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NewOrderDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExistsValid(entity = Order.class, field = "orderName", message = "{IfExistsValid.order.name}")
    String orderName,
    String notes,

    @NotNull(message = "{NotBlank.validation.text}")
    Date orderDate,

    @ManyToOne
    @JoinColumn(name = "id_user")
    @JsonIgnoreProperties({"id", "orders", "roles"})
    User user
) {
}
