package com.multi.shop.api.multi_shop_api.payments.dtos;

import java.util.List;

public record NewTransactionDTO(
    List<ProductItemDTO> productItems,
    String status
) {}
