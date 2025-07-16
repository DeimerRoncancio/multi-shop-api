package com.multi.shop.api.multi_shop_api.common.services;

import org.springframework.stereotype.Service;

import com.multi.shop.api.multi_shop_api.common.repositories.CustomRepository;

@Service
public class CustomServiceImpl implements CustomService {
    private final CustomRepository repository;

    public CustomServiceImpl(CustomRepository repository) {
        this.repository = repository;
    }

    @Override
    public Long ifExists(Object value, String entity, String fieldName) {
        return repository.ifExists(value, entity, fieldName);
    }

    @Override
    public Long ifExistsUpdate(String value, String id, String entity, String fieldName) {
        return repository.ifExistsUpdate(value, id, entity, fieldName);
    }
}
