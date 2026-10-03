package com.multi.shop.transactions.mappers;

import com.multi.shop.transactions.dtos.CustomerAddressDTO;
import com.multi.shop.transactions.entities.Address;
import com.multi.shop.transactions.entities.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface TransactionMapper {
    void updateAddress(CustomerAddressDTO addressDTO, @MappingTarget Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    ShippingAddress toShippingAddress(CustomerAddressDTO addressDTO);

    CustomerAddressDTO toCustomerAddressDTO(ShippingAddress shippingAddress);
}
