package com.multi.shop.api.multi_shop_api.payments.mappers;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;


@Mapper
public interface TransactionMapper {
    TransactionMapper MAPPER = Mappers.getMapper(TransactionMapper.class);

    Address updateAddress(CustomerAddressDTO addressDTO, @MappingTarget Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    ShippingAddress toShippingAddress(CustomerAddressDTO addressDTO);

    CustomerAddressDTO toCustomerAddressDTO(ShippingAddress shippingAddress);
}
