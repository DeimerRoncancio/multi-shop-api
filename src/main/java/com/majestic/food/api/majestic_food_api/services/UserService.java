package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.entities.User;

public interface UserService {

    List<User> findAll();

    Optional<User> findOne(String id);

    User save(User user);

    Optional<User> update(String id, User user);

    Optional<User> delete(String id);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(Long number);
}
