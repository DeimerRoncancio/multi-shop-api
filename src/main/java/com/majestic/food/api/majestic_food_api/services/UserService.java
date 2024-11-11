package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.entities.dtos.UserCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.UserUpdateDTO;

public interface UserService {

    List<User> findAll();

    Optional<User> findOne(String id);

    User save(UserCreateDTO user);

    Optional<User> update(String id, UserUpdateDTO user);

    Optional<User> delete(String id);
}
