package com.multi.shop.api.multi_shop_api.services;

import java.util.Optional;

public interface CustomService {

    Optional<?> findByCustomField(String entity, String fieldName, Object value);

    Long ifExistsCustomField(String entity, String fieldName, Object value);
}
