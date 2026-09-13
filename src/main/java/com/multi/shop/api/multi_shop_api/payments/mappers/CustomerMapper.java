package com.multi.shop.api.multi_shop_api.payments.mappers;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerCheckoutDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CustomerMapper {
    CustomerMapper MAPPER = Mappers.getMapper(CustomerMapper.class);

    default CustomerSummaryDTO toCustomerSummaryDTO(Customer customer) {
        String userNames = customer.isGuest()
            ? customer.getGuest().getUserNames()
            : customer.getUser().getName();

        String userEmail = customer.isGuest()
            ? customer.getGuest().getUserEmail()
            : customer.getUser().getEmail();

        String userPhone = customer.isGuest()
            ? customer.getGuest().getUserPhone()
            : customer.getUser().getPhoneNumber() == null
                ? null
                : customer.getUser().getPhoneNumber().toString();

        return new CustomerSummaryDTO(userNames, userEmail, userPhone);
    }

    default List<CustomerAddressDTO> toCustomerAddressDTOs(Customer customer) {
        return customer.getAddress().stream()
            .map(address -> new CustomerAddressDTO(
                address.getAddressName(),
                address.getAddress(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getAddressNumber()
            ))
            .toList();
    }

    default CustomerCheckoutDTO toCustomerCheckoutDTO(Customer customer) {
        CustomerSummaryDTO summary = toCustomerSummaryDTO(customer);

        return new CustomerCheckoutDTO(
            summary.userNames(),
            summary.userEmail(),
            summary.userPhone(),
            toCustomerAddressDTOs(customer),
            null
        );
    }
}
