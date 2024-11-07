package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Role;
import com.majestic.food.api.majestic_food_api.repositories.RoleRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository repository;

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
