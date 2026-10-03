package com.multi.shop.catalog.common.services;

public interface CustomService {
    boolean ifExists(Object value, String entity, String fieldName);

    boolean ifExistsUpdate(String value, String id, String entity, String fieldName);

    boolean ifExistsById(String id, String entity);
}
