package com.multi.shop.api.multi_shop_api.identity.auth.dtos;

import com.multi.shop.api.multi_shop_api.identity.users.entities.User;

public record UserInfo(
    String identifier,
    User user
) {
}
