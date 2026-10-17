package com.multi.shop.transactions.mappers;

import com.multi.shop.transactions.dtos.PurchaseDTO;
import com.multi.shop.transactions.entities.Transaction;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {TransactionMapper.class, CheckoutMapper.class}
)
public interface PurchaseMapper {
    @Mapping(target = "date", expression = "java(transaction.getTransactionDate() != null ? transaction.getTransactionDate() : transaction.getCreatedAt())")
    @Mapping(target = "items", source = "productItems")
    PurchaseDTO toPurchaseDTO(Transaction transaction);

    List<PurchaseDTO> toPurchaseDTOs(List<Transaction> transactions);
}
