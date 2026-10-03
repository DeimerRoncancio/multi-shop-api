package com.multi.shop.transactions.mappers;

import com.multi.shop.transactions.dtos.CheckoutProductItemDTO;
import com.multi.shop.transactions.dtos.CheckoutSummaryDTO;
import com.multi.shop.transactions.entities.ProductItem;
import com.multi.shop.transactions.entities.Transaction;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {CustomerMapper.class, TransactionMapper.class, CustomerSummaryMapper.class}
)
public interface CheckoutMapper {
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerSummary")
    @Mapping(target = "addresses", source = "customer", qualifiedByName = "customerAddresses")
    @Mapping(target = "selectedAddress", source = "shippingAddress")
    @Mapping(target = "items", source = "productItems")
    CheckoutSummaryDTO toCheckoutSummaryDTO(Transaction transaction);

    @Mapping(target = "id", source = "productId")
    @Mapping(target = "price", source = "unitPrice")
    CheckoutProductItemDTO toCheckoutProductItemDTO(ProductItem item);
}
