package com.multi.shop.catalog.common.services;

import org.springframework.stereotype.Service;
import com.multi.shop.catalog.common.repositories.CustomRepository;

@Service
public class CustomServiceImpl implements CustomService {
    private final CustomRepository repository;

    public CustomServiceImpl(CustomRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean ifExists(Object value, String entity, String fieldName) {
        return repository.ifExists(value, entity, fieldName) == 0;
    }

    @Override
    public boolean ifExistsUpdate(String value, String id, String entity, String fieldName) {
        return repository.ifExistsUpdate(value, id, entity, fieldName) == 0;
    }

    @Override
    public boolean ifExistsById(String id, String entity) {
        return repository.ifExistsById(id, entity) == 0;
    }
}
