package com.multi.shop.api.multi_shop_api.catalog.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface CatalogApi {
    Optional<CatalogProduct> findProduct(String id);

    Map<String, CatalogProduct> findProducts(Collection<String> ids);

    long countProducts();

    long countCategories();
}
