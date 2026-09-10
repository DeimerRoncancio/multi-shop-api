package com.multi.shop.api.multi_shop_api.payments.mappers;

import com.multi.shop.api.multi_shop_api.payments.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TransactionMapper {
    TransactionMapper MAPPER = Mappers.getMapper(TransactionMapper.class);

    List<ProductItem> productItemDTOtoProductItem(List<ProductItemDTO> productItems);
}
