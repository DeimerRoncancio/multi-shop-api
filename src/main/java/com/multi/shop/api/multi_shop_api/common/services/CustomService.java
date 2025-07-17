package com.multi.shop.api.multi_shop_api.common.services;

public interface CustomService {
    Long ifExists(Object value, String entity, String fieldName);

    Long ifExistsUpdate(String value, String id, String entity, String fieldName);
}
