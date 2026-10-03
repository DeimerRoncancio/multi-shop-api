package com.multi.shop.transactions.catalog;

public record CatalogProduct(
    String id,
    String productName,
    String description,
    Long price
) {
}
