package com.multi.shop.api.multi_shop_api.transactions.mappers;

import com.multi.shop.api.multi_shop_api.identity.api.Account;
import com.multi.shop.api.multi_shop_api.identity.api.IdentityApi;
import com.multi.shop.api.multi_shop_api.transactions.dtos.CustomerSummaryDTO;
import com.multi.shop.api.multi_shop_api.transactions.entities.Customer;
import com.multi.shop.api.multi_shop_api.transactions.entities.Guest;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class CustomerSummaryMapper {
    private final IdentityApi identityApi;

    public CustomerSummaryMapper(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Named("customerSummary")
    public CustomerSummaryDTO toCustomerSummaryDTO(Customer customer) {
        if (customer == null) return null;

        if (customer.isGuest()) {
            Guest guest = customer.getGuest();
            return new CustomerSummaryDTO(guest.getUserNames(), guest.getUserEmail(), guest.getUserPhone());
        }

        return identityApi.findByEmail(customer.getUserEmail())
            .map(CustomerSummaryMapper::toCustomerSummaryDTO)
            .orElseGet(() -> new CustomerSummaryDTO(null, customer.getUserEmail(), null));
    }

    private static CustomerSummaryDTO toCustomerSummaryDTO(Account account) {
        String phone = account.phoneNumber() == null ? null : account.phoneNumber().toString();
        return new CustomerSummaryDTO(account.name(), account.email(), phone);
    }
}
