package com.majestic.food.api.majestic_food_api.controllers;

import com.majestic.food.api.majestic_food_api.entities.Order;
import com.majestic.food.api.majestic_food_api.services.OrderService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/orders")
public class OrderController {

    @Autowired
    private OrderService service;

    @GetMapping
    public List<Order> viewAll() {
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> view(@PathVariable String id) {
        Optional<Order> orderDb = service.findOne(id);

        if (orderDb.isPresent())
            return ResponseEntity.ok().body(orderDb.get());
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Order order) {
        Optional<Order> orderDb = service.update(id, order);
        
        if (orderDb.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(orderDb.get());
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<Order> orderDb = service.delete(id);

        if (orderDb.isPresent())
            return ResponseEntity.ok().build();

        return ResponseEntity.notFound().build();
    }
}
