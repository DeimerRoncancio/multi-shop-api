package com.multi.shop.api.multi_shop_api.auth.mappers;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterUserDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserResponseDTO;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "admin", expression = "java(isAdmin)")
    RegisterUserDTO requestDTOtoNotAdmin(RegisterUserDTO user, boolean isAdmin);

    UserResponseDTO userToUserResponse(User user);
}
