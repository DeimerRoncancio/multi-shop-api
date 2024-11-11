package com.majestic.food.api.majestic_food_api.helpers;

import java.util.Optional;

public interface CustomService {

    Optional<?> findByCustomField(String entity, String fieldName, Object value);

    Long ifExistsCustomField(String entity, String fieldName, Object value);
}
