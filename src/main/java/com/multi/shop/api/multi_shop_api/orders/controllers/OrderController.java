package com.multi.shop.api.multi_shop_api.orders.controllers;

import jakarta.validation.Valid;
import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multi.shop.api.multi_shop_api.orders.entities.Order;
import com.multi.shop.api.multi_shop_api.orders.dtos.NewOrderDTO;
import com.multi.shop.api.multi_shop_api.orders.dtos.UpdateOrderDTO;
import com.multi.shop.api.multi_shop_api.orders.services.OrderService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/orders")
@CrossOrigin(originPatterns = "*")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Order> viewAll() {
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Order> view(@PathVariable String id) {
        Optional<Order> orderDb = service.findOne(id);

        return orderDb.map(order -> ResponseEntity.ok().body(order))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<NewOrderDTO> create(@Valid @RequestBody NewOrderDTO order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(order));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UpdateOrderDTO> update(@Valid @RequestBody UpdateOrderDTO order,
    @PathVariable String id) {
        Optional<Order> orderDb = service.update(id, order);
        orderDb.orElseThrow(() -> new NotFoundException("Order not found"));

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Optional<Order> orderDb = service.delete(id);
        orderDb.orElseThrow(() -> new NotFoundException("Order not found"));

        return ResponseEntity.ok().build();
    }
}
