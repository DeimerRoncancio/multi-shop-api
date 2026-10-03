package com.multi.shop.catalog.services;

public record CatalogProduct(
    String id,
    String productName,
    String description,
    Long price
) {
}
