package com.multi.shop.api.multi_shop_api.mappers;

import com.multi.shop.api.multi_shop_api.auth.RegisterRequest;
import com.multi.shop.api.multi_shop_api.entities.Role;
import com.multi.shop.api.multi_shop_api.entities.User;
import com.multi.shop.api.multi_shop_api.entities.dtos.UserUpdateRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-01T18:48:30-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
public class UserMapperImpl implements UserMapper {

    @Override
    public User userCreateDTOtoUser(RegisterRequest dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setName( dto.getName() );
        user.setImageUser( dto.getImageUser() );
        user.setSecondName( dto.getSecondName() );
        user.setLastnames( dto.getLastnames() );
        user.setPhoneNumber( dto.getPhoneNumber() );
        user.setGender( dto.getGender() );
        user.setEmail( dto.getEmail() );
        user.setPassword( dto.getPassword() );
        List<Role> list = dto.getRoles();
        if ( list != null ) {
            user.setRoles( new ArrayList<Role>( list ) );
        }
        user.setAdmin( dto.isAdmin() );

        return user;
    }

    @Override
    public void toUpdateUser(UserUpdateRequest dto, User user) {
        if ( dto == null ) {
            return;
        }

        user.setName( dto.getName() );
        user.setSecondName( dto.getSecondName() );
        user.setLastnames( dto.getLastnames() );
        user.setPhoneNumber( dto.getPhoneNumber() );
        user.setGender( dto.getGender() );
        user.setEmail( dto.getEmail() );
        user.setPassword( dto.getPassword() );
        user.setAdmin( dto.isAdmin() );
    }
}
