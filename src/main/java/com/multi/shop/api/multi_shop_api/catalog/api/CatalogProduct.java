package com.multi.shop.api.multi_shop_api.catalog.api;

public record CatalogProduct(
    String id,
    String productName,
    String description,
    Long price
) {
}
