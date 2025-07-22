package com.multi.shop.api.multi_shop_api.auth.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.common.validation.IfExists;
import com.multi.shop.api.multi_shop_api.common.validation.ImageFormat;
import com.multi.shop.api.multi_shop_api.common.validation.NotEmptyFile;
import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.users.entities.Role;
import com.multi.shop.api.multi_shop_api.auth.validation.SizeConstraint;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record RegisterUserDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    String name,

    @JsonIgnoreProperties("id")
    Image imageUser,
    String secondName,
    String lastnames,

    @Transient
    @NotEmptyFile
    @ImageFormat(maxSize = 1024 * 1024)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    MultipartFile profileImage,

    @IfExists(message = "{IfExists.validation}", field = "phoneNumber", entity = "User")
    Long phoneNumber,
    String gender,

    @Email
    @IfExists(message = "{IfExists.validation}", field ="email", entity = "User")
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
}
