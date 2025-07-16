package com.multi.shop.api.multi_shop_api.auth.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.common.validation.IfExistsValid;
import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.users.entities.Role;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.auth.validation.SizeConstraint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    String name,

    @JsonIgnoreProperties("id")
    Image imageUser,
    String secondName,
    String lastnames,

    @IfExistsValid(entity = User.class, field = "phoneNumber", message = "{IfExistsValid.user.phone}")
    Long phoneNumber,
    String gender,

    @Email
    @IfExistsValid(entity = User.class, field = "email")
    @NotBlank(message = "{NotBlank.validation.text}")
    String email,

    @NotBlank(message = "{NotBlank.validation.text}")
    @SizeConstraint(min = 8, max = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password,

    @JsonIgnoreProperties({"users", "id"})
    List<Role> roles,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    boolean admin
) {
    public static RegisterRequestDTO onlyUser(RegisterRequestDTO user){
        return new RegisterRequestDTO(
            user.name(),
            user.imageUser(),
            user.secondName(),
            user.lastnames(),
            user.phoneNumber(),
            user.gender(),
            user.email(),
            user.password(),
            user.roles(),
            false
        );
    }
}
