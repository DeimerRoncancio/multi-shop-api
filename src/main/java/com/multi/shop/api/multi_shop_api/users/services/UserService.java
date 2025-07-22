package com.multi.shop.api.multi_shop_api.users.services;

import java.util.Optional;

import com.multi.shop.api.multi_shop_api.users.dtos.PasswordDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterUserDTO;
import com.multi.shop.api.multi_shop_api.users.entities.User;

public interface UserService {
    Page<UserResponseDTO> findAll(Pageable pageable);

    Optional<UserResponseDTO> findOne(String id);

    RegisterUserDTO save(RegisterUserDTO user);

    Optional<UserDTO> update(String id, UserDTO user);

    Optional<User> updatePassword(String id, PasswordDTO passwordInfo);

    User updateProfileImage(String id, MultipartFile file);

    Optional<User> delete(String id);

    int usersSize();
}
