package com.multi.shop.api.multi_shop_api.auth.dtos;

public record LoginRequestDTO(
    String identifier,
    String password
) {
}
