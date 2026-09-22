package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Guest;
import com.multi.shop.api.multi_shop_api.payments.entities.ShippingAddress;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.enums.TransactionStatus;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @BeforeEach
    void useTheRealIdentityLookup() {
        lenient().doCallRealMethod().when(userRepository).findByIdentity(any());
    }

    @Test
    void createsAGuestCustomerWithoutSavedAddresses() {
        Transaction transaction = authorizedTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(repository.save(transaction)).thenReturn(transaction);
        when(customersRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Transaction> result = service.addUserToTransaction(guestDto(), "transaction-id", CHECKOUT_ACCESS_TOKEN, null);

        assertThat(result).contains(transaction);
        Customer customer = transaction.getCustomer();
        assertThat(customer.getUser()).isNull();
        assertThat(customer.getGuest().getUserNames()).isEqualTo("Guest User");
        assertThat(customer.getGuest().getUserEmail()).isEqualTo("guest@example.com");
        assertThat(customer.getGuest().getUserPhone()).isEqualTo("3001234567");
        assertThat(customer.getAddress()).isEmpty();
        ShippingAddress snapshot = transaction.getShippingAddress();
        assertThat(snapshot.getAddressName()).isEqualTo("Casa");
        assertThat(snapshot.getAddress()).isEqualTo("Calle 1");
        assertThat(snapshot.getCity()).isEqualTo("Bogota");
        assertThat(snapshot.getState()).isEqualTo("Cundinamarca");
        assertThat(snapshot.getCountry()).isEqualTo("Colombia");
        assertThat(snapshot.getAddressNumber()).isEqualTo("3001234567");
        assertThat(snapshot.getTransaction()).isSameAs(transaction);
        verifyNoInteractions(addressRepository);
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).findByPhoneNumber(any());
    }

    @Test
    void givesEachGuestOrderItsOwnCustomerEvenWithTheSameEmail() {
        Transaction first = authorizedTransaction();
        Transaction second = authorizedTransaction();
        when(repository.findById("first")).thenReturn(Optional.of(first));
        when(repository.findById("second")).thenReturn(Optional.of(second));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(customersRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UserTransactionDTO otherGuest = new UserTransactionDTO("Other Guest", "guest@example.com", "3000000000", address("Casa"));

        service.addUserToTransaction(guestDto(), "first", CHECKOUT_ACCESS_TOKEN, null);
        service.addUserToTransaction(otherGuest, "second", CHECKOUT_ACCESS_TOKEN, null);

        assertThat(first.getCustomer()).isNotSameAs(second.getCustomer());
        assertThat(first.getCustomer().getGuest().getUserNames()).isEqualTo("Guest User");
        assertThat(second.getCustomer().getGuest().getUserNames()).isEqualTo("Other Guest");
    }

    @Test
    void reusesAnExistingGuestWithTheSameNormalizedData() {
        Transaction transaction = authorizedTransaction();
        Customer existing = new Customer();
        existing.setId("customer-id");
        existing.setGuest(new Guest("guest-id", "Guest User", "guest@example.com", "3001234567"));
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(repository.save(transaction)).thenReturn(transaction);
        when(customersRepository.findFirstByGuest_UserNamesAndGuest_UserEmailAndGuest_UserPhone(
            "Guest User", "guest@example.com", "3001234567")).thenReturn(Optional.of(existing));
        UserTransactionDTO messyDto = new UserTransactionDTO("  Guest   User ", " Guest@Example.COM ", " 3001234567 ", address("Casa"));

        service.addUserToTransaction(messyDto, "transaction-id", CHECKOUT_ACCESS_TOKEN, null);

        assertThat(transaction.getCustomer()).isSameAs(existing);
        verify(customersRepository, never()).save(any());
    }

    @Test
    void createsANewGuestInsteadOfChangingAnExistingOne() {
        Transaction transaction = authorizedTransaction();
        Customer previous = new Customer();
        previous.setGuest(new Guest("guest-id", "Guest User", "guest@example.com", "3000000000"));
        transaction.setCustomer(previous);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(repository.save(transaction)).thenReturn(transaction);
        when(customersRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.addUserToTransaction(guestDto(), "transaction-id", CHECKOUT_ACCESS_TOKEN, null);

        assertThat(transaction.getCustomer()).isNotSameAs(previous);
        assertThat(transaction.getCustomer().getGuest().getUserPhone()).isEqualTo("3001234567");
        assertThat(previous.getGuest().getUserPhone()).isEqualTo("3000000000");
    }

    @Test
    void doesNotLinkAGuestToTheAccountThatOwnsTheEmail() {
        Transaction transaction = authorizedTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(repository.save(transaction)).thenReturn(transaction);
        when(customersRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UserTransactionDTO dto = new UserTransactionDTO("Someone Else", "registered@example.com", "3000000000", address("Casa"));

        service.addUserToTransaction(dto, "transaction-id", CHECKOUT_ACCESS_TOKEN, null);

        assertThat(transaction.getCustomer().getUser()).isNull();
        assertThat(transaction.getCustomer().getGuest().getUserEmail()).isEqualTo("registered@example.com");
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).findByPhoneNumber(any());
    }

    @Test
    void linksASignedInUserAndSavesTheAddressInTheirAccount() {
        Transaction transaction = authorizedTransaction();
        User user = registeredUser();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(repository.save(transaction)).thenReturn(transaction);
        when(userRepository.findByEmail("registered@example.com")).thenReturn(Optional.of(user));
        when(customersRepository.findByUserEmail("registered@example.com")).thenReturn(Optional.empty());

        service.addUserToTransaction(guestDto(), "transaction-id", CHECKOUT_ACCESS_TOKEN, "registered@example.com");

        Customer customer = transaction.getCustomer();
        assertThat(customer.getUser()).isSameAs(user);
        assertThat(customer.getAddress()).singleElement().satisfies(address -> {
            assertThat(address.getAddressName()).isEqualTo("Casa");
            assertThat(address.getCustomer()).isSameAs(customer);
        });
        verify(customersRepository).save(customer);

        customer.getAddress().get(0).setAddress("Calle modificada");
        assertThat(transaction.getShippingAddress().getAddress()).isEqualTo("Calle 1");
    }

    @Test
    void findsAUserWhoSignedInWithTheirPhoneNumber() {
        Transaction transaction = authorizedTransaction();
        User user = registeredUser();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(repository.save(transaction)).thenReturn(transaction);
        when(userRepository.findByPhoneNumber(3001234567L)).thenReturn(Optional.of(user));
        when(customersRepository.findByUserEmail("registered@example.com")).thenReturn(Optional.empty());

        service.addUserToTransaction(guestDto(), "transaction-id", CHECKOUT_ACCESS_TOKEN, "3001234567");

        assertThat(transaction.getCustomer().getUser()).isSameAs(user);
    }

    @Test
    void updatesASavedAddressWithTheSameNameInsteadOfDuplicatingIt() {
        Transaction transaction = authorizedTransaction();
        Customer customer = new Customer();
        customer.setId("customer-id");
        customer.setUser(registeredUser());
        Address saved = new Address();
        saved.setAddressName("Casa");
        saved.setAddress("Calle vieja");
        saved.setCustomer(customer);
        customer.getAddress().add(saved);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(repository.save(transaction)).thenReturn(transaction);
        when(userRepository.findByEmail("registered@example.com")).thenReturn(Optional.of(customer.getUser()));
        when(customersRepository.findByUserEmail("registered@example.com")).thenReturn(Optional.of(customer));
        when(addressRepository.findByCustomerAndAddressName(customer, "Casa")).thenReturn(Optional.of(saved));

        service.addUserToTransaction(guestDto(), "transaction-id", CHECKOUT_ACCESS_TOKEN, "registered@example.com");

        assertThat(customer.getAddress()).containsExactly(saved);
        assertThat(saved.getAddress()).isEqualTo("Calle 1");
        verify(customersRepository, never()).save(any());
    }

    @Test
    void returnsTheSavedAddressesOfTheSignedInUser() {
        Customer customer = new Customer();
        Address saved = new Address();
        saved.setAddressName("Casa");
        saved.setAddress("Calle 1");
        customer.getAddress().add(saved);
        when(userRepository.findByEmail("registered@example.com")).thenReturn(Optional.of(registeredUser()));
        when(customersRepository.findByUserEmail("registered@example.com")).thenReturn(Optional.of(customer));

        assertThat(service.getSavedAddresses("registered@example.com"))
            .singleElement()
            .satisfies(address -> assertThat(address.address()).isEqualTo("Calle 1"));
    }

    @Test
    void returnsNoSavedAddressesWithoutAKnownUser() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThat(service.getSavedAddresses("nobody@example.com")).isEmpty();
        assertThat(service.getSavedAddresses(null)).isEmpty();
    }

    @Test
    void refusesToAddAUserWithoutAValidAccessToken() {
        Transaction transaction = authorizedTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThat(service.addUserToTransaction(guestDto(), "transaction-id", "wrong-token", null)).isEmpty();
        assertThat(service.addUserToTransaction(guestDto(), "transaction-id", null, null)).isEmpty();

        verify(repository, never()).save(any());
        assertThat(transaction.getShippingAddress()).isNull();
    }

    @Test
    void refusesToChangeTheCustomerOfAPaidTransaction() {
        Transaction transaction = authorizedTransaction();
        transaction.setStatus(TransactionStatus.APPROVED);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> service.addUserToTransaction(guestDto(), "transaction-id", CHECKOUT_ACCESS_TOKEN, null))
            .isInstanceOf(ResponseStatusException.class);
        verify(repository, never()).save(any());
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

    private Transaction authorizedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setCheckoutAccessTokenDigest(new CheckoutAccessToken().digest(CHECKOUT_ACCESS_TOKEN));
        return transaction;
    }

    private User registeredUser() {
        User user = new User();
        user.setName("Registered User");
        user.setEmail("registered@example.com");
        user.setPhoneNumber(3001234567L);
        return user;
    }

    private UserTransactionDTO guestDto() {
        return new UserTransactionDTO("Guest User", "guest@example.com", "3001234567", address("Casa"));
    }

    private CustomerAddressDTO address(String name) {
        return new CustomerAddressDTO(name, "Calle 1", "Bogota", "Cundinamarca", "Colombia", "3001234567");
    }
}
