package com.majestic.food.api.majestic_food_api.controllers;

import com.majestic.food.api.majestic_food_api.entities.Role;
import com.majestic.food.api.majestic_food_api.services.RoleService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/roles")
public class RoleController {

    @Autowired
    private RoleService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Role> viewAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> view(@PathVariable String id) {
        Optional<Role> roleDb = service.findOne(id);

        if (roleDb.isPresent())
            return ResponseEntity.ok().body(roleDb.get());
        
        return ResponseEntity.notFound().build();
    }
}
