package com.multi.shop.api.multi_shop_api.payments.dtos;

import java.util.List;

public record CustomerCheckoutDTO(
    String userNames,
    String userEmail,
    String userPhone,
    List<CustomerAddressDTO> addresses,
    CustomerAddressDTO selectedAddress
) {}
