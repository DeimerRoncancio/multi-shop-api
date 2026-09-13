package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerCheckoutDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Guest;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.repositories.AddressRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.CustomersRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceImplTest {
    @Mock
    private PaymentsRepository repository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomersRepository customersRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentsServiceImpl service;

    @Test
    void returnsAddressesForTheLinkedCustomerWhenEmailMatches() {
        Customer customer = guestCustomer("guest@example.com");
        Address address = new Address();
        address.setAddressName("Casa");
        address.setAddress("Calle 1");
        address.setCity("Bogota");
        address.setState("Cundinamarca");
        address.setCountry("Colombia");
        address.setAddressNumber("3001234567");
        customer.getAddress().add(address);
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        Optional<CustomerCheckoutDTO> result = service.getCheckoutCustomer(
            "transaction-id",
            "guest@example.com"
        );

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().addresses()).hasSize(1);
        assertThat(result.orElseThrow().addresses().get(0).addressName()).isEqualTo("Casa");
    }

    @Test
    void rejectsAnEmailThatDoesNotMatchTheLinkedCustomer() {
        Transaction transaction = new Transaction();
        transaction.setCustomer(guestCustomer("linked@example.com"));
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        Optional<CustomerCheckoutDTO> result = service.getCheckoutCustomer(
            "transaction-id",
            "other@example.com"
        );

        assertThat(result).isEmpty();
        verify(customersRepository, never()).findByUser_EmailOrGuest_UserEmail(
            "other@example.com",
            "other@example.com"
        );
    }

    @Test
    void findsExistingCustomerForAnUnlinkedTransactionWithoutMutatingData() {
        Transaction transaction = new Transaction();
        Customer customer = guestCustomer("guest@example.com");
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(customersRepository.findByUser_EmailOrGuest_UserEmail(
            "guest@example.com",
            "guest@example.com"
        )).thenReturn(Optional.of(customer));

        Optional<CustomerCheckoutDTO> result = service.getCheckoutCustomer(
            "transaction-id",
            "guest@example.com"
        );

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().addresses()).isEmpty();
        verify(repository, never()).save(transaction);
        verify(customersRepository, never()).save(customer);
    }

    private Customer guestCustomer(String email) {
        Guest guest = new Guest();
        guest.setUserNames("Guest User");
        guest.setUserEmail(email);
        guest.setUserPhone("3001234567");
        Customer customer = new Customer();
        customer.setGuest(guest);
        return customer;
    }
}
