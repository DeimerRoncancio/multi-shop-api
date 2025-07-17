package com.multi.shop.api.multi_shop_api.users.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import com.multi.shop.api.multi_shop_api.users.entities.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    String name,
    String secondName,
    String lastnames,

    @IfExists(message = "{IfExists.validation}", field = "phoneNumber", entity = "User")
    Long phoneNumber,
    String gender,

    @Email
    @NotBlank(message = "{NotBlank.validation.text}")
    @IfExists(message = "{IfExists.validation}", field = "email", entity = "User")
    String email,

    @JsonIgnoreProperties({"users", "id"})
    List<Role> roles,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    boolean admin
) {
}
