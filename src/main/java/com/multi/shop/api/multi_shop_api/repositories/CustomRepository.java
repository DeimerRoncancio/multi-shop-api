package com.multi.shop.api.multi_shop_api.repositories;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@Component
public class CustomRepository {
    
    private final EntityManager entityManager;
    
    public CustomRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public Optional<?> findByCustomField(String entity, String fieldName, Object value) {
        String SELECT_JPQL = String.format("SELECT e FROM %s e WHERE e.%s = :value", entity, fieldName);

        List<?> objQuery = entityManager.createQuery(SELECT_JPQL)
            .setParameter("value", value)
            .getResultList();
        
       return objQuery.isEmpty() ? Optional.empty() : Optional.of(objQuery.get(0));
    }

    public Long ifExistsCustomField(String entity, String fieldName, Object value) {
        String EXISTS_JPQL = String.format("SELECT COUNT(e) FROM %s e WHERE e.%s = :value", entity, fieldName);

        return entityManager.createQuery(EXISTS_JPQL, Long.class)
            .setParameter("value", value)
            .getSingleResult();
    }
}

