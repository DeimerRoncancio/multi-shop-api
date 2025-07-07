package com.multi.shop.api.multi_shop_api.auth.dtos;

import com.multi.shop.api.multi_shop_api.users.entities.User;

public record UserInfo(
    String identifier,
    User user
) {
}
