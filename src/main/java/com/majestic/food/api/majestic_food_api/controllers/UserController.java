package com.majestic.food.api.majestic_food_api.controllers;

import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.services.UserService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/users")
public class UserController {

    @Autowired
    private UserService service;
    
    @GetMapping
    public List<User> viewAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> view(@PathVariable String id) {
        Optional<User> product = service.findOne(id);

        if (product.isPresent())
            return ResponseEntity.ok().body(product.get());
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody User user,@PathVariable String id) {
        Optional<User> userOptional = service.update(id, user);
        
        if (userOptional.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(userOptional.get());
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<User> optionalUser = service.delete(id);

        if (optionalUser.isPresent())
            return ResponseEntity.ok().body("Eliminado correctamente");
        
        return ResponseEntity.notFound().build();
    }
}
