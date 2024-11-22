package com.majestic.food.api.majestic_food_api.controllers;

import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.entities.dtos.UserUpdateDTO;
import com.majestic.food.api.majestic_food_api.services.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/app/users")
@CrossOrigin(originPatterns = "*")
public class UserController {

    @Autowired
    private UserService service;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> viewAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> view(@PathVariable String id) {
        Optional<User> product = service.findOne(id);

        if (product.isPresent())
            return ResponseEntity.ok().body(product.get());
        
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@Valid @RequestBody UserUpdateDTO user, BindingResult result, @PathVariable String id) {
        if (result.hasFieldErrors())
            return validate(result);
        
        Optional<User> userOptional = service.update(id, user);
        
        if (userOptional.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateDTO user, BindingResult result, @PathVariable String id) {
        user.setAdmin(false);

        return update(user, result, id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<User> optionalUser = service.delete(id);

        if (optionalUser.isPresent())
            return ResponseEntity.ok().body("Eliminado correctamente");
        
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
