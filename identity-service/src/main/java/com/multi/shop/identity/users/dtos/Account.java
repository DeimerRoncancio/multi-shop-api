package com.multi.shop.identity.users.dtos;

public record Account(
    String name,
    String email,
    Long phoneNumber
) {
}
