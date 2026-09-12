package com.multi.shop.api.multi_shop_api.payments.dtos;

public record CustomerAddressDTO(
    String addressName,
    String address,
    String city,
    String state,
    String country,
    String addressNumber
) {}
