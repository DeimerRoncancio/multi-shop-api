package com.multi.shop.api.multi_shop_api.common.repositories;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Component
public class CustomRepository {
    private final EntityManager entityManager;
    
    public CustomRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public Long ifExists(Object value, String entity, String fieldName) {
        String EXISTS_JPQL = String.format(
            "SELECT COUNT(e) FROM %s e WHERE e.%s = :value",
            entity, fieldName
        );

        return entityManager.createQuery(EXISTS_JPQL, Long.class)
            .setParameter("value", value)
            .getSingleResult();
    }

    public Long ifExistsUpdate(String value, String id, String entity, String fieldName) {
        String jpql = String.format(
            "SELECT COUNT(e) FROM %s e WHERE e.%s = :value AND e.id != :id",
            entity, fieldName
        );

        return entityManager.createQuery(jpql, Long.class)
            .setParameter("value", value)
            .setParameter("id", id)
            .getSingleResult();
    }

    public Long ifExistsById(String id, String entity) {
        String jpql = String.format("select count(e) from %s e where e.id=:id", entity);
        return entityManager.createQuery(jpql, Long.class).setParameter("id", id).getSingleResult();
    }
}
