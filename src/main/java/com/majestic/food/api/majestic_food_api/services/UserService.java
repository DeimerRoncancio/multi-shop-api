package com.majestic.food.api.majestic_food_api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.majestic.food.api.majestic_food_api.auth.RegisterRequest;
import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.entities.dtos.UserUpdateRequest;

public interface UserService {

    List<User> findAll();

    Optional<User> findOne(String id);

    RegisterRequest save(RegisterRequest user, MultipartFile file);

    Optional<User> update(String id, UserUpdateRequest user);

    User updateProfileImage(User user, MultipartFile file);

    Optional<User> delete(String id);
}
