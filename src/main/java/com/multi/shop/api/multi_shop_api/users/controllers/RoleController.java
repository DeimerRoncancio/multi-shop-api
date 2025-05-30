package com.multi.shop.api.multi_shop_api.users.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.multi.shop.api.multi_shop_api.users.entities.Role;
import com.multi.shop.api.multi_shop_api.users.services.RoleService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/roles")
@CrossOrigin(originPatterns = "*")
public class RoleController {
    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Role> viewAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> view(@PathVariable String id) {
        Optional<Role> roleDb = service.findOne(id);

        return roleDb.map(role ->
            ResponseEntity.ok().body(role)
        ).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
