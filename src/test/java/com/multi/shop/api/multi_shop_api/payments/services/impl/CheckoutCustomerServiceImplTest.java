package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerCheckoutDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Guest;
import com.multi.shop.api.multi_shop_api.payments.entities.ShippingAddress;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.mappers.TransactionMapper;
import com.multi.shop.api.multi_shop_api.payments.repositories.AddressRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.CustomersRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.security.CheckoutAccessToken;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutCustomerServiceImplTest {
    private static final String CHECKOUT_ACCESS_TOKEN = "checkout-access-token";

    @Mock
    private PaymentsRepository repository;
    @Mock
    private CustomersRepository customersRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private CheckoutAccessToken checkoutAccessToken = new CheckoutAccessToken();

    @InjectMocks
    private CheckoutCustomerServiceImpl service;

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
        transaction.setShippingAddress(shippingAddress());
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        Optional<CustomerCheckoutDTO> result = service.getCheckoutCustomer(
            "transaction-id",
            "guest@example.com"
        );

        assertThat(result).isPresent();
        CustomerCheckoutDTO checkoutCustomer = result.orElseThrow();
        assertThat(checkoutCustomer.userNames()).isEqualTo("Guest User");
        assertThat(checkoutCustomer.userEmail()).isEqualTo("guest@example.com");
        assertThat(checkoutCustomer.userPhone()).isEqualTo("3001234567");
        assertThat(checkoutCustomer.addresses()).containsExactly(new CustomerAddressDTO(
            "Casa",
            "Calle 1",
            "Bogota",
            "Cundinamarca",
            "Colombia",
            "3001234567"
        ));
        assertThat(checkoutCustomer.selectedAddress()).isEqualTo(new CustomerAddressDTO(
            "Oficina",
            "Carrera 7",
            "Medellin",
            "Antioquia",
            "Colombia",
            "3017654321"
        ));
    }

    @Test
    void mapsARegisteredCustomerPhoneThroughTheService() {
        User user = new User();
        user.setName("Registered User");
        user.setEmail("registered@example.com");
        user.setPhoneNumber(3001234567L);
        Customer customer = new Customer();
        customer.setUser(user);
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        Optional<CustomerCheckoutDTO> result = service.getCheckoutCustomer(
            "transaction-id",
            "registered@example.com"
        );

        assertThat(result).contains(new CustomerCheckoutDTO(
            "Registered User",
            "registered@example.com",
            "3001234567",
            List.of(),
            null
        ));
    }

    @Test
    void preservesANullPhoneForARegisteredCustomer() {
        User user = new User();
        user.setName("Registered User");
        user.setEmail("registered@example.com");
        Customer customer = new Customer();
        customer.setUser(user);
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        Optional<CustomerCheckoutDTO> result = service.getCheckoutCustomer(
            "transaction-id",
            "registered@example.com"
        );

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().userPhone()).isNull();
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
        assertThat(result.orElseThrow().selectedAddress()).isNull();
        verify(repository, never()).save(transaction);
        verify(customersRepository, never()).save(customer);
    }

    @Test
    void persistsAnIndependentShippingAddressSnapshotWhenAddingAUser() {
        Transaction transaction = authorizedTransaction();
        CustomerAddressDTO selectedAddress = new CustomerAddressDTO(
            "Casa",
            "Calle 1",
            "Bogota",
            "Cundinamarca",
            "Colombia",
            "3001234567"
        );
        UserTransactionDTO dto = new UserTransactionDTO(
            "Guest User",
            "guest@example.com",
            "3001234567",
            selectedAddress
        );
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(customersRepository.findByUser_EmailOrGuest_UserEmail(
            "guest@example.com",
            "guest@example.com"
        )).thenReturn(Optional.empty());
        when(userRepository.findByEmail("guest@example.com")).thenReturn(Optional.empty());
        when(repository.save(transaction)).thenReturn(transaction);

        Optional<Transaction> result = service.addUserToTransaction(dto, "transaction-id", CHECKOUT_ACCESS_TOKEN);

        assertThat(result).contains(transaction);
        verify(repository).save(transaction);
        ShippingAddress snapshot = transaction.getShippingAddress();
        assertThat(snapshot.getAddressName()).isEqualTo("Casa");
        assertThat(snapshot.getAddress()).isEqualTo("Calle 1");
        assertThat(snapshot.getCity()).isEqualTo("Bogota");
        assertThat(snapshot.getState()).isEqualTo("Cundinamarca");
        assertThat(snapshot.getCountry()).isEqualTo("Colombia");
        assertThat(snapshot.getAddressNumber()).isEqualTo("3001234567");
        assertThat(snapshot.getTransaction()).isSameAs(transaction);
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(savedTransaction ->
            savedTransaction.getShippingAddress().getTransaction() == savedTransaction
        ));

        transaction.getCustomer().getAddress().get(0).setAddress("Calle modificada");

        assertThat(transaction.getShippingAddress().getAddress()).isEqualTo("Calle 1");
    }

    @Test
    void mapsShippingAddressAsTheOwningTransactionOneToOneEntity() throws NoSuchFieldException {
        Table table = ShippingAddress.class.getAnnotation(Table.class);
        assertThat(table.name()).isEqualTo("transaction_address");

        OneToOne ownerRelationship = ShippingAddress.class
            .getDeclaredField("transaction")
            .getAnnotation(OneToOne.class);
        JoinColumn transactionJoinColumn = ShippingAddress.class
            .getDeclaredField("transaction")
            .getAnnotation(JoinColumn.class);
        assertThat(ownerRelationship.fetch()).isEqualTo(FetchType.LAZY);
        assertThat(ownerRelationship.optional()).isFalse();
        assertThat(transactionJoinColumn.name()).isEqualTo("transaction_id");
        assertThat(transactionJoinColumn.nullable()).isFalse();
        assertThat(transactionJoinColumn.unique()).isTrue();

        OneToOne inverseRelationship = Transaction.class
            .getDeclaredField("shippingAddress")
            .getAnnotation(OneToOne.class);
        assertThat(inverseRelationship.mappedBy()).isEqualTo("transaction");
        assertThat(inverseRelationship.fetch()).isEqualTo(FetchType.EAGER);
        assertThat(inverseRelationship.orphanRemoval()).isTrue();
        assertThat(inverseRelationship.cascade()).containsExactly(CascadeType.ALL);
    }

    @Test
    void refusesToAddAUserWithoutAValidAccessToken() {
        Transaction transaction = authorizedTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThat(service.addUserToTransaction(guestDto(), "transaction-id", "wrong-token")).isEmpty();
        assertThat(service.addUserToTransaction(guestDto(), "transaction-id", null)).isEmpty();

        verify(repository, never()).save(any());
        assertThat(transaction.getCustomer()).isNull();
    }

    @Test
    void refusesToChangeTheCustomerOfAPaidTransaction() {
        Transaction transaction = authorizedTransaction();
        transaction.setStatus("APPROVED");
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> service.addUserToTransaction(guestDto(), "transaction-id", CHECKOUT_ACCESS_TOKEN))
            .isInstanceOf(ResponseStatusException.class);
        verify(repository, never()).save(any());
    }

    private Transaction authorizedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setCheckoutAccessTokenDigest(new CheckoutAccessToken().digest(CHECKOUT_ACCESS_TOKEN));
        return transaction;
    }

    private UserTransactionDTO guestDto() {
        return new UserTransactionDTO(
            "Guest User",
            "guest@example.com",
            "3001234567",
            new CustomerAddressDTO("Casa", "Calle 1", "Bogota", "Cundinamarca", "Colombia", "3001234567")
        );
    }

    private ShippingAddress shippingAddress() {
        CustomerAddressDTO address = new CustomerAddressDTO(
            "Oficina",
            "Carrera 7",
            "Medellin",
            "Antioquia",
            "Colombia",
            "3017654321"
        );
        return TransactionMapper.MAPPER.toShippingAddress(address);
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
