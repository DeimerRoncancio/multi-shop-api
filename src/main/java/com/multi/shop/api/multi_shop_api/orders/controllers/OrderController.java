package com.multi.shop.api.multi_shop_api.orders.controllers;

import com.multi.shop.api.multi_shop_api.orders.repositories.OrderRepository;
import jakarta.validation.Valid;

import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multi.shop.api.multi_shop_api.orders.entities.Order;
import com.multi.shop.api.multi_shop_api.orders.entities.dtos.NewOrderDTO;
import com.multi.shop.api.multi_shop_api.orders.entities.dtos.UpdateOrderDTO;
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
    private final OrderRepository repository;

    public OrderController(OrderService service, OrderRepository repository) {
        this.service = service;
        this.repository = repository;
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
        handleObjectError(order, id, result);

        if (result.hasErrors())
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

        result.getGlobalErrors().forEach(err -> {
            errors.put(err.getObjectName(), "El campo " + err.getObjectName() + " " + err.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    public void handleObjectError(UpdateOrderDTO order, String id, BindingResult result) {
        Optional<Order> orderOp = repository.findByOrderName(order.getOrderName());
        Optional<Order> currentOrder = repository.findById(id);

        if (orderOp.isPresent()) {
            if (currentOrder.isEmpty()) return;
            if (!currentOrder.get().getOrderName().equals(order.getOrderName()))
                setError("orderName", "tiene un valor existente", result);
        }
    }

    public void setError(String objName, String defaultMessage, BindingResult result) {
        result.addError(new ObjectError(objName, defaultMessage));
    }
}
