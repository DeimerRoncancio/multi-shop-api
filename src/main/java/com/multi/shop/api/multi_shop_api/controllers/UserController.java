package com.multi.shop.api.multi_shop_api.controllers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.entities.User;
import com.multi.shop.api.multi_shop_api.entities.dtos.UserUpdateRequest;
import com.multi.shop.api.multi_shop_api.services.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/app/users")
@CrossOrigin(originPatterns = "*")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }
    
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
    public ResponseEntity<?> update(@Valid @RequestBody UserUpdateRequest user, BindingResult result, 
    @PathVariable String id) {
        if (result.hasFieldErrors())
            return validate(result);
        
        Optional<User> userOptional = service.update(id, user);
        
        if (userOptional.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateRequest user, BindingResult result, 
    @PathVariable String id) {
        user.setAdmin(false);

        return update(user, result, id);
    }

    @PutMapping("/update/profile-image/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateProfileImage(@PathVariable String id, @RequestPart MultipartFile file) {
        Optional<User> optionalUser = service.findOne(id);

        if (optionalUser.isPresent()) {
            User user = service.updateProfileImage(optionalUser.get(), file);
            return ResponseEntity.status(HttpStatus.CREATED).body(user.getProfileImage().getImageUrl());
        }
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<User> optionalUser = service.delete(id);

        if (optionalUser.isPresent())
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
