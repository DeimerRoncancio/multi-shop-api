package com.multi.shop.api.multi_shop_api.common.repositories;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Component
public class CustomRepository {
    private final EntityManager entityManager;
    
    public CustomRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public Long ifExistsCustomField(String entity, String fieldName, Object value) {
        String EXISTS_JPQL = String.format("SELECT COUNT(e) FROM %s e WHERE e.%s = :value", entity, fieldName);

        return entityManager.createQuery(EXISTS_JPQL, Long.class)
            .setParameter("value", value)
            .getSingleResult();
    }
}

