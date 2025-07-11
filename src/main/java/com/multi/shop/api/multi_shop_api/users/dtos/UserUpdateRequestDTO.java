package com.multi.shop.api.multi_shop_api.users.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.users.entities.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    String name,
    String secondName,
    String lastnames,
    Long phoneNumber,
    String gender,

    @Email
    @NotBlank(message = "{NotBlank.validation.text}")
    String email,

    @JsonIgnoreProperties({"users", "id"})
    List<Role> roles,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    boolean admin
) {
}
