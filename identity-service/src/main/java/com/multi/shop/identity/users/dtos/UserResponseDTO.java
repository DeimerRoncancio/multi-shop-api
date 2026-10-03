package com.multi.shop.identity.users.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.identity.media.StoredImage;

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
