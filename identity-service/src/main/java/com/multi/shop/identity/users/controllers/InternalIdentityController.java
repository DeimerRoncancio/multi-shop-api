package com.multi.shop.identity.users.controllers;

import com.multi.shop.identity.users.dtos.Account;
import com.multi.shop.identity.users.entities.User;
import com.multi.shop.identity.users.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/internal")
public class InternalIdentityController {
    private final UserRepository repository;

    public InternalIdentityController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/accounts")
    @Transactional(readOnly = true)
    public ResponseEntity<Account> findAccount(
        @RequestParam(required = false) String identity,
        @RequestParam(required = false) String email
    ) {
        Optional<User> user = identity != null
            ? repository.findByIdentity(identity)
            : email != null ? repository.findByEmail(email) : Optional.empty();

        return user
            .map(found -> new Account(found.getName(), found.getEmail(), found.getPhoneNumber()))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public Map<String, Long> stats() {
        return Map.of("users", repository.count());
    }
}
