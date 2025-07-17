package com.multi.shop.api.multi_shop_api.users.services;

import java.util.Optional;

import com.multi.shop.api.multi_shop_api.users.dtos.PasswordDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterRequestDTO;
import com.multi.shop.api.multi_shop_api.users.entities.User;

public interface UserService {
    Page<User> findAll(Pageable pageable);

    Optional<User> findOne(String id);

    RegisterRequestDTO save(RegisterRequestDTO user);

    Optional<UserDTO> update(String id, UserDTO user);

    Optional<?> updatePassword(String id, PasswordDTO passwordInfo) throws Exception;

    User updateProfileImage(User user, MultipartFile file);

    Optional<User> delete(String id);

    int usersSize();
}
