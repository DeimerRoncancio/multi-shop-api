package com.multi.shop.api.multi_shop_api.users.services;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.auth.entities.RegisterRequest;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.entities.dtos.UserUpdateRequest;

public interface UserService {
    List<User> findAll();

    Optional<User> findOne(String id);

    RegisterRequest save(RegisterRequest user, MultipartFile file);

    Optional<User> update(String id, UserUpdateRequest user);

    User updateProfileImage(User user, MultipartFile file);

    Optional<User> delete(String id);

    int usersSize();
}
