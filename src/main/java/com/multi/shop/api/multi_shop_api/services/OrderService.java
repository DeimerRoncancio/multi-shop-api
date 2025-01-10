package com.multi.shop.api.multi_shop_api.services;

import java.util.List;
import java.util.Optional;

import com.multi.shop.api.multi_shop_api.entities.Order;
import com.multi.shop.api.multi_shop_api.entities.dtos.NewOrderDTO;
import com.multi.shop.api.multi_shop_api.entities.dtos.UpdateOrderDTO;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findOne(String id);

    NewOrderDTO save(NewOrderDTO user);

    Optional<Order> update(String id, UpdateOrderDTO order);

    Optional<Order> delete(String id);
}
