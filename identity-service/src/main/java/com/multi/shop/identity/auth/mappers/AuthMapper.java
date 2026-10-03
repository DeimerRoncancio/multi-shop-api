package com.multi.shop.identity.auth.mappers;

import com.multi.shop.identity.auth.dtos.RegisterUserDTO;
import com.multi.shop.identity.users.dtos.UserResponseDTO;
import com.multi.shop.identity.users.entities.User;
import com.multi.shop.identity.users.mappers.ProfileImageMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ProfileImageMapper.class)
public interface AuthMapper {
    @Mapping(target = "admin", expression = "java(isAdmin)")
    RegisterUserDTO requestDTOtoNotAdmin(RegisterUserDTO user, boolean isAdmin);

    @Mapping(target = "imageUser", source = "profileImageId", qualifiedByName = "profileImage")
    UserResponseDTO userToUserResponse(User user);
}
