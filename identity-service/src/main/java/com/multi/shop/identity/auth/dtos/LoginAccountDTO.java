package com.multi.shop.identity.auth.dtos;

public record LoginAccountDTO(
    String identifier,
    String password
) {
}
