package com.multi.shop.api.multi_shop_api.auth.mappers;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterRequestDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserResponseDTO;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthMapper {
    AuthMapper MAPPER = Mappers.getMapper(AuthMapper.class);

    @Mapping(target = "admin", expression = "java(isAdmin)")
    RegisterRequestDTO requestDTOtoNotAdmin(RegisterRequestDTO user, boolean isAdmin);

    UserResponseDTO userToUserResponse(User user);
}
