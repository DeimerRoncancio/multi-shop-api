package com.multi.shop.api.multi_shop_api.users.services;

import java.util.List;
import java.util.Optional;

import com.multi.shop.api.multi_shop_api.users.entities.dtos.UpdatePasswordRequest;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterRequestDTO;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.entities.dtos.UserUpdateRequest;

public interface UserService {
    List<User> findAll();

    Optional<User> findOne(String id);

    RegisterRequestDTO save(RegisterRequestDTO user, MultipartFile file);

    Optional<User> update(String id, UserUpdateRequest user);

    Optional<?> updatePassword(String id, UpdatePasswordRequest passwordInfo) throws Exception;

    User updateProfileImage(User user, MultipartFile file);

    Optional<User> delete(String id);

    int usersSize();
}
