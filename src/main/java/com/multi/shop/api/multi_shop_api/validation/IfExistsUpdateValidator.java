package com.multi.shop.api.multi_shop_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.multi.shop.api.multi_shop_api.services.CustomService;

import java.lang.reflect.Field;
import java.util.Optional;

@Component
public class IfExistsUpdateValidator implements ConstraintValidator<IfExistsUpdate, Object> {

    @PersistenceContext
    private EntityManager entityManager;
    private Class<?> entity;
    private String field;
    
    private static final Logger logger = LoggerFactory.getLogger(IfExistsUpdateValidator.class);
    private final CustomService service;

    public IfExistsUpdateValidator(CustomService service) {
        this.service = service;
    }

    @Override
    public void initialize(IfExistsUpdate annotation) {
        entity = annotation.entity();
        field = annotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (entityManager == null)
           return false;

        Optional<?> entityList = service.findByCustomField(entity.getSimpleName(), field, value);
        String idURI = getIdByURI();

        if (!entityList.isPresent())
            return true;
        
        Object obj = entityList.get();

        try {
            Field field = obj.getClass().getDeclaredField("id");
            field.setAccessible(true);
            String id = (String) field.get(obj);

            return id.equals(idURI);
                
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            logger.error("Exception to try acces to id: " + e);
        }

        return true;
    }

    public String getIdByURI() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;

        if (requestAttributes != null)
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
        
        return request != null ? request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/') + 1) : null;
    }
}
