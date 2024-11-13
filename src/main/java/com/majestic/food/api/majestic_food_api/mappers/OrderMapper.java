package com.majestic.food.api.majestic_food_api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.majestic.food.api.majestic_food_api.entities.Order;
import com.majestic.food.api.majestic_food_api.entities.dtos.OrderCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.OrderUpdateDTO;

@Mapper
public interface OrderMapper {

    OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore =true)
    Order orderCreateDTOtoOrder(OrderCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    void toUpdateOrder(OrderUpdateDTO dto, @MappingTarget Order order);
}
