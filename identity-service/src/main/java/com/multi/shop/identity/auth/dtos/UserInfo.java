package com.multi.shop.identity.auth.dtos;

import com.multi.shop.identity.users.entities.User;

public record UserInfo(
    String identifier,
    User user
) {
}
