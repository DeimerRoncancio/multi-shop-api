package com.multi.shop.api.multi_shop_api.common.services;

public interface CustomService {
    boolean ifExists(Object value, String entity, String fieldName);

    boolean ifExistsUpdate(String value, String id, String entity, String fieldName);

    boolean ifExistsById(String id, String entity);
}
