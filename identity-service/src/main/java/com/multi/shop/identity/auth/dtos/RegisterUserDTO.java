package com.multi.shop.identity.auth.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.identity.common.validation.IfExists;
import com.multi.shop.identity.common.validation.ImageFormat;
import com.multi.shop.identity.media.StoredImage;
import com.multi.shop.identity.users.entities.Role;
import com.multi.shop.identity.auth.validation.SizeConstraint;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record RegisterUserDTO(
    @NotBlank(message = "{NotBlank.validation.text}")
    String name,

    @JsonIgnoreProperties("id")
    StoredImage imageUser,
    String secondName,
    String lastnames,

    @Transient
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
    Boolean admin
) {
}
