package com.multi.shop.catalog.repositories;

public interface CategoryStats {
    String getId();
    String getCategoryName();
    long getProductCount();
    Long getFromPrice();
}
