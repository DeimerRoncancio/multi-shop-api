package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.auth.RegisterRequest;
import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.entities.dtos.UserUpdateRequest;

public interface UserService {

    List<User> findAll();

    Optional<User> findOne(String id);

    User save(RegisterRequest user);

    Optional<User> update(String id, UserUpdateRequest user);

    Optional<User> delete(String id);
}
