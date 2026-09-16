package com.multi.shop.api.multi_shop_api.payments.mappers;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerCheckoutDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = TransactionMapper.class)
public interface CustomerMapper {
    CustomerMapper MAPPER = Mappers.getMapper(CustomerMapper.class);

    @Mapping(target = "userNames", source = "customer", qualifiedByName = "customerNames")
    @Mapping(target = "userEmail", source = "customer", qualifiedByName = "customerEmail")
    @Mapping(target = "userPhone", source = "customer", qualifiedByName = "customerPhone")
    @Mapping(target = "addresses", source = "customer", qualifiedByName = "customerAddresses")
    @Mapping(target = "selectedAddress", source = "transaction.shippingAddress")
    CustomerCheckoutDTO toCustomerCheckoutDTO(Customer customer, Transaction transaction);

    @Mapping(target = "userNames", source = ".", qualifiedByName = "customerNames")
    @Mapping(target = "userEmail", source = ".", qualifiedByName = "customerEmail")
    @Mapping(target = "userPhone", source = ".", qualifiedByName = "customerPhone")
    CustomerSummaryDTO toCustomerSummaryDTO(Customer customer);

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

    @Named("customerAddresses")
    default List<CustomerAddressDTO> toCustomerAddressDTOs(Customer customer) {
        return customer == null ? List.of() : toCustomerAddressDTOs(customer.getAddress());
    }

    List<CustomerAddressDTO> toCustomerAddressDTOs(List<Address> addresses);
}
