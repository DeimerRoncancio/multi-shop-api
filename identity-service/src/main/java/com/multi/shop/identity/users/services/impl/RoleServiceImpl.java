package com.multi.shop.identity.users.services.impl;

import com.multi.shop.identity.users.services.RoleService;
import org.springframework.transaction.annotation.Transactional;

import com.multi.shop.identity.users.entities.Role;
import com.multi.shop.identity.users.repositories.RoleRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository repository;

    public RoleServiceImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findOne(String id) {
        return repository.findById(id);
    }
}
