package com.multi.shop.api.multi_shop_api.users.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.images.entities.Image;

public record UserResponseDTO(
    String id,
    String name,

    @JsonIgnoreProperties("id")
    Image profileImage,
    String secondName,
    String lastnames,
    Long phoneNumber,
    String gender,
    String email,
    boolean admin,
    boolean enabled
) {
}
