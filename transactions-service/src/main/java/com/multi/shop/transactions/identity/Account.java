package com.multi.shop.transactions.identity;

public record Account(
    String name,
    String email,
    Long phoneNumber
) {
}
