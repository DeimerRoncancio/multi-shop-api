package com.multi.shop.transactions.mappers;

import com.multi.shop.transactions.dtos.CustomerAddressDTO;
import com.multi.shop.transactions.entities.Address;
import com.multi.shop.transactions.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Named("customerAddresses")
    default List<CustomerAddressDTO> toCustomerAddressDTOs(Customer customer) {
        return customer == null ? List.of() : toCustomerAddressDTOs(customer.getAddress());
    }

    List<CustomerAddressDTO> toCustomerAddressDTOs(List<Address> addresses);
}
