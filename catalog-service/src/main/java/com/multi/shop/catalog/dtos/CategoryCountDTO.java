package com.multi.shop.catalog.dtos;

public record CategoryCountDTO(
    String categoryName,
    long productCount
) {}
