package com.multi.shop.transactions.catalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface CatalogApi {
    Optional<CatalogProduct> findProduct(String id);

    Map<String, CatalogProduct> findProducts(Collection<String> ids);
}
