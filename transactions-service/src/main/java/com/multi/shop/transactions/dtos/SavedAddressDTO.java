package com.multi.shop.transactions.dtos;

public record SavedAddressDTO(
    String id,
    String addressName,
    String address,
    String city,
    String state,
    String country,
    String addressNumber
) {}
