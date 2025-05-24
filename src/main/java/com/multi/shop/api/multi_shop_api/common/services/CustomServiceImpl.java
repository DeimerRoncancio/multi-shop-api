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
    public Long ifExistsCustomField(String entity, String fieldName, Object value) {
        return repository.ifExistsCustomField(entity, fieldName, value);
    }
}
