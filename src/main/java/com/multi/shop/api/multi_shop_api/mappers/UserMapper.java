package com.multi.shop.api.multi_shop_api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.multi.shop.api.multi_shop_api.auth.RegisterRequest;
import com.multi.shop.api.multi_shop_api.entities.User;
import com.multi.shop.api.multi_shop_api.entities.dtos.UserUpdateRequest;

@Mapper
public interface UserMapper {

    UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    User userCreateDTOtoUser(RegisterRequest dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "imageUser", ignore = true)
    void toUpdateUser(UserUpdateRequest dto, @MappingTarget User user);
}
