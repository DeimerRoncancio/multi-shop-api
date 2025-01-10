package com.multi.shop.api.multi_shop_api.controllers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multi.shop.api.multi_shop_api.entities.Order;
import com.multi.shop.api.multi_shop_api.entities.dtos.NewOrderDTO;
import com.multi.shop.api.multi_shop_api.entities.dtos.UpdateOrderDTO;
import com.multi.shop.api.multi_shop_api.services.OrderService;

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
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        if (orderDb.isPresent())
            return ResponseEntity.ok().body(orderDb.get());
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody NewOrderDTO order, BindingResult result) {
        if (result.hasFieldErrors())
            return validate(result);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(order));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> update(@Valid @RequestBody UpdateOrderDTO order, BindingResult result, 
    @PathVariable String id) {
        if (result.hasFieldErrors())
            return validate(result);
        
        Optional<Order> orderDb = service.update(id, order);
        
        if (orderDb.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<Order> orderDb = service.delete(id);

        if (orderDb.isPresent())
            return ResponseEntity.ok().build();

        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<?> validate(BindingResult result) {
        Map<String, String> errors = new HashMap<> ();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }
}
