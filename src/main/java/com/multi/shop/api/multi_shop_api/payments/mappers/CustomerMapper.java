package com.multi.shop.api.multi_shop_api.payments.mappers;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "userNames", source = ".", qualifiedByName = "customerNames")
    @Mapping(target = "userEmail", source = ".", qualifiedByName = "customerEmail")
    @Mapping(target = "userPhone", source = ".", qualifiedByName = "customerPhone")
    CustomerSummaryDTO toCustomerSummaryDTO(Customer customer);

    @Named("customerAddresses")
    default List<CustomerAddressDTO> toCustomerAddressDTOs(Customer customer) {
        return customer == null ? List.of() : toCustomerAddressDTOs(customer.getAddress());
    }

    @Named("customerNames")
    default String customerNames(Customer customer) {
        return customer.isGuest()
            ? customer.getGuest().getUserNames()
            : customer.getUser().getName();
    }

    @Named("customerEmail")
    default String customerEmail(Customer customer) {
        return customer.isGuest()
            ? customer.getGuest().getUserEmail()
            : customer.getUser().getEmail();
    }

    @Named("customerPhone")
    default String customerPhone(Customer customer) {
        return customer.isGuest()
            ? customer.getGuest().getUserPhone()
            : customer.getUser().getPhoneNumber() == null
                ? null
                : customer.getUser().getPhoneNumber().toString();
    }

    List<CustomerAddressDTO> toCustomerAddressDTOs(List<Address> addresses);
}
