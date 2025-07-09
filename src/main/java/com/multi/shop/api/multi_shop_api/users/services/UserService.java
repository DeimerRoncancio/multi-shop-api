package com.multi.shop.api.multi_shop_api.users.services;

import java.util.Optional;

import com.multi.shop.api.multi_shop_api.users.dtos.UpdatePasswordRequestDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterRequestDTO;
import com.multi.shop.api.multi_shop_api.users.entities.User;

public interface UserService {
    Page<User> findAll(Pageable pageable);

    Optional<User> findOne(String id);

    RegisterRequestDTO save(RegisterRequestDTO user, MultipartFile file);

    Optional<User> update(String id, UserUpdateRequestDTO user);

    Optional<?> updatePassword(String id, UpdatePasswordRequestDTO passwordInfo) throws Exception;

    User updateProfileImage(User user, MultipartFile file);

    Optional<User> delete(String id);

    int usersSize();
}
