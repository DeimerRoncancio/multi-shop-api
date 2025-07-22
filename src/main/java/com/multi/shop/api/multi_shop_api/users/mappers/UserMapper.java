package com.multi.shop.api.multi_shop_api.users.mappers;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterUserDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.multi.shop.api.multi_shop_api.users.entities.User;

@Mapper
public interface UserMapper {
    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    User registerDTOtoUser(RegisterUserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "imageUser", ignore = true)
    @Mapping(target = "password", ignore = true)
    void toUpdateUser(UserDTO dto, @MappingTarget User user);

    @Mapping(target = "admin", expression = "java(isAdmin)")
    UserDTO userDTOtoOrAdmin(UserDTO user, boolean isAdmin);

    RegisterUserDTO userToRegisterDTO(User user);

    UserResponseDTO userToResponseDTO(User user);

    UserDTO userToUserDTO(User user);
}
