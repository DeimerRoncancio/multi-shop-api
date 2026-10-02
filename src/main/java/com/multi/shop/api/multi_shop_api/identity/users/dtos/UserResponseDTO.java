package com.multi.shop.api.multi_shop_api.identity.users.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.media.api.StoredImage;

public record UserResponseDTO(
    String id,
    String name,

    @JsonIgnoreProperties("id")
    StoredImage imageUser,
    String secondName,
    String lastnames,
    Long phoneNumber,
    String gender,
    String email,
    boolean admin,
    boolean enabled
) {
}
