package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.entities.Role;

public interface RoleService {

    List<Role> findAll();

    Optional<Role> findOne(String id);
}
