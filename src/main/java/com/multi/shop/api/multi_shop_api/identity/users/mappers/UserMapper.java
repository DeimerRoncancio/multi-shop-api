package com.multi.shop.api.multi_shop_api.identity.users.mappers;

import com.multi.shop.api.multi_shop_api.identity.auth.dtos.RegisterUserDTO;
import com.multi.shop.api.multi_shop_api.identity.users.dtos.UserDTO;
import com.multi.shop.api.multi_shop_api.identity.users.dtos.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.multi.shop.api.multi_shop_api.identity.users.entities.User;

@Mapper(componentModel = "spring", uses = ProfileImageMapper.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "profileImageId", ignore = true)
    User registerDTOtoUser(RegisterUserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "profileImageId", ignore = true)
    @Mapping(target = "password", ignore = true)
    void toUpdateUser(UserDTO dto, @MappingTarget User user);

    @Mapping(target = "admin", expression = "java(isAdmin)")
    UserDTO userDTOtoOrAdmin(UserDTO user, boolean isAdmin);

    @Mapping(target = "imageUser", source = "profileImageId", qualifiedByName = "profileImage")
    RegisterUserDTO userToRegisterDTO(User user);

    @Mapping(target = "imageUser", source = "profileImageId", qualifiedByName = "profileImage")
    UserResponseDTO userToResponseDTO(User user);

    UserDTO userToUserDTO(User user);
}
