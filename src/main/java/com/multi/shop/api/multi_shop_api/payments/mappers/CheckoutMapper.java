package com.multi.shop.api.multi_shop_api.payments.mappers;

import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutProductItemDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {CustomerMapper.class, TransactionMapper.class})
public interface CheckoutMapper {
    CheckoutMapper MAPPER = Mappers.getMapper(CheckoutMapper.class);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "addresses", source = "customer", qualifiedByName = "customerAddresses")
    @Mapping(target = "selectedAddress", source = "shippingAddress")
    @Mapping(target = "items", source = "productItems")
    CheckoutSummaryDTO toCheckoutSummaryDTO(Transaction transaction);

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "price", source = "product.price")
    CheckoutProductItemDTO toCheckoutProductItemDTO(ProductItem item);
}
