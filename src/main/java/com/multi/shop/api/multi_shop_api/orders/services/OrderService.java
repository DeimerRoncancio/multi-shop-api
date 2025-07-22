package com.multi.shop.api.multi_shop_api.orders.services;

import java.util.List;
import java.util.Optional;

import com.multi.shop.api.multi_shop_api.orders.dtos.OrderResponseDTO;
import com.multi.shop.api.multi_shop_api.orders.entities.Order;
import com.multi.shop.api.multi_shop_api.orders.dtos.NewOrderDTO;
import com.multi.shop.api.multi_shop_api.orders.dtos.UpdateOrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<OrderResponseDTO> findAll(Pageable pageable);

    Optional<OrderResponseDTO> findOne(String id);

    NewOrderDTO save(NewOrderDTO user);

    Optional<Order> update(String id, UpdateOrderDTO order);

    Optional<Order> delete(String id);
}
