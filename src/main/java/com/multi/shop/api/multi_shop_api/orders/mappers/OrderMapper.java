package com.multi.shop.api.multi_shop_api.orders.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.multi.shop.api.multi_shop_api.orders.entities.Order;
import com.multi.shop.api.multi_shop_api.orders.entities.dtos.NewOrderDTO;
import com.multi.shop.api.multi_shop_api.orders.entities.dtos.UpdateOrderDTO;

@Mapper
public interface OrderMapper {

    OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore =true)
    Order orderCreateDTOtoOrder(NewOrderDTO dto);

    @Mapping(target = "id", ignore = true)
    void toUpdateOrder(UpdateOrderDTO dto, @MappingTarget Order order);
}
