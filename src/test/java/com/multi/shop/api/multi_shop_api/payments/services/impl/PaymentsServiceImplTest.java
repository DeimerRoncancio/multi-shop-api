package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerCheckoutDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.TransactionAccessDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Guest;
import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import com.multi.shop.api.multi_shop_api.payments.entities.ShippingAddress;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.mappers.TransactionMapper;
import com.multi.shop.api.multi_shop_api.payments.repositories.AddressRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.CustomersRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceImplTest {
    private static final String CHECKOUT_ACCESS_TOKEN = "checkout-access-token";

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
    void createsTransactionWithAnOpaqueAccessTokenAndPersistsOnlyItsDigest() {
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId("transaction-id");
            return transaction;
        });

        TransactionAccessDTO access = service.createTransaction(new NewTransactionDTO(List.of(), "pending"));

        assertThat(access.transactionId()).isEqualTo("transaction-id");
        assertThat(access.checkoutAccessToken()).hasSize(43);
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(transaction ->
            transaction.getCheckoutAccessTokenDigest() != null
                && !transaction.getCheckoutAccessTokenDigest().equals(access.checkoutAccessToken())
        ));
    }

    @Test
    void returnsACompletePersistedCheckoutSummaryWithoutMutatingTheTransaction() {
        Customer customer = guestCustomer("guest@example.com");
        Address address = new Address();
        address.setAddressName("Casa");
        address.setAddress("Calle 1");
        address.setCity("Bogota");
        address.setState("Cundinamarca");
        address.setCountry("Colombia");
        address.setAddressNumber("3001234567");
        customer.getAddress().add(address);

        Product product = new Product();
        product.setId("product-id");
        product.setProductName("Cafe");
        product.setPrice(25000L);
        ProductItem productItem = new ProductItem();
        productItem.setProduct(product);
        productItem.setQuantity(2);

        Transaction transaction = new Transaction();
        transaction.setId("transaction-id");
        transaction.setStatus("Pending");
        transaction.setTotalPrice(50000L);
        transaction.setCustomer(customer);
        transaction.setShippingAddress(shippingAddress());
        transaction.getProductItems().add(productItem);
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        Optional<CheckoutSummaryDTO> result = service.getCheckoutSummary(
            "transaction-id",
            CHECKOUT_ACCESS_TOKEN
        );

        assertThat(result).isPresent();
        CheckoutSummaryDTO summary = result.orElseThrow();
        assertThat(summary.transactionId()).isEqualTo("transaction-id");
        assertThat(summary.status()).isEqualTo("Pending");
        assertThat(summary.totalPrice()).isEqualTo(50000L);
        assertThat(summary.customer().userNames()).isEqualTo("Guest User");
        assertThat(summary.customer().userEmail()).isEqualTo("guest@example.com");
        assertThat(summary.customer().userPhone()).isEqualTo("3001234567");
        assertThat(summary.addresses()).containsExactly(new CustomerAddressDTO(
            "Casa", "Calle 1", "Bogota", "Cundinamarca", "Colombia", "3001234567"
        ));
        assertThat(summary.selectedAddress()).isEqualTo(new CustomerAddressDTO(
            "Oficina", "Carrera 7", "Medellin", "Antioquia", "Colombia", "3017654321"
        ));
        assertThat(summary.items()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo("product-id");
            assertThat(item.productName()).isEqualTo("Cafe");
            assertThat(item.price()).isEqualTo(25000L);
            assertThat(item.quantity()).isEqualTo(2);
        });
        assertThat(transaction.getTotalPrice()).isEqualTo(50000L);
        assertThat(transaction.getStatus()).isEqualTo("Pending");
        assertThat(transaction.getCustomer()).isSameAs(customer);
        verify(repository, never()).save(transaction);
    }

    @Test
    void returnsCheckoutSummaryWithNullCustomerAndEmptyAddresses() {
        Transaction transaction = new Transaction();
        transaction.setId("transaction-id");
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        CheckoutSummaryDTO summary = service.getCheckoutSummary(
            "transaction-id",
            CHECKOUT_ACCESS_TOKEN
        ).orElseThrow();

        assertThat(summary.customer()).isNull();
        assertThat(summary.addresses()).isEmpty();
    }

    @Test
    void returnsCheckoutSummaryWithNullSelectedAddress() {
        Transaction transaction = new Transaction();
        transaction.setId("transaction-id");
        transaction.setCustomer(guestCustomer("guest@example.com"));
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        CheckoutSummaryDTO summary = service.getCheckoutSummary(
            "transaction-id",
            CHECKOUT_ACCESS_TOKEN
        ).orElseThrow();

        assertThat(summary.selectedAddress()).isNull();
    }

    @Test
    void returnsEmptyCheckoutSummaryWhenTransactionDoesNotExist() {
        when(repository.findById("missing-id")).thenReturn(Optional.empty());

        Optional<CheckoutSummaryDTO> result = service.getCheckoutSummary(
            "missing-id",
            CHECKOUT_ACCESS_TOKEN
        );

        assertThat(result).isEmpty();
    }

    @Test
    void rejectsCheckoutSummaryWithoutAccessToken() {
        Transaction transaction = new Transaction();
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        Optional<CheckoutSummaryDTO> result = service.getCheckoutSummary("transaction-id", null);

        assertThat(result).isEmpty();
    }

    @Test
    void rejectsCheckoutSummaryWithInvalidAccessToken() {
        Transaction transaction = new Transaction();
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        Optional<CheckoutSummaryDTO> result = service.getCheckoutSummary(
            "transaction-id",
            "wrong-access-token"
        );

        assertThat(result).isEmpty();
    }

    @Test
    void rejectsLegacyCheckoutWithoutStoredDigest() {
        Transaction transaction = new Transaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThat(service.getCheckoutSummary("transaction-id", CHECKOUT_ACCESS_TOKEN)).isEmpty();
        assertThat(service.getCheckoutSummary("transaction-id", null)).isEmpty();
    }

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
        Transaction transaction = new Transaction();
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

        Optional<Transaction> result = service.addUserToTransaction(dto, "transaction-id");

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

    @Test
    void buildsThePaymentSessionFromTheStoredItemsAndCatalogPrices() {
        ReflectionTestUtils.setField(service, "stripeCurrency", "cop");
        Transaction transaction = new Transaction();
        transaction.setId("transaction-id");
        transaction.getProductItems().add(productItem("Hamburguesa", "Clásica con papas", 38000L, 2));
        transaction.getProductItems().add(productItem("Limonada", " ", 9000L, 1));

        SessionCreateParams params = service.buildSessionParams(transaction);

        assertThat(params.getLineItems()).hasSize(2);
        SessionCreateParams.LineItem burger = params.getLineItems().get(0);
        assertThat(burger.getQuantity()).isEqualTo(2L);
        assertThat(burger.getPriceData().getUnitAmount()).isEqualTo(3_800_000L);
        assertThat(burger.getPriceData().getCurrency()).isEqualTo("cop");
        assertThat(burger.getPriceData().getProductData().getName()).isEqualTo("Hamburguesa");
        assertThat(params.getLineItems().get(1).getPriceData().getProductData().getDescription())
            .isEqualTo("Limonada");
        assertThat(params.getClientReferenceId()).isEqualTo("transaction-id");
        assertThat(params.getMetadata()).containsEntry("transactionId", "transaction-id");
    }

    @Test
    void skipsItemsWithoutAProductOrPrice() {
        Transaction transaction = new Transaction();
        transaction.getProductItems().add(productItem("Hamburguesa", "Clásica", 38000L, 1));
        transaction.getProductItems().add(productItem("Sin precio", "Borrador", null, 1));
        ProductItem orphan = new ProductItem();
        orphan.setQuantity(3);
        transaction.getProductItems().add(orphan);

        SessionCreateParams params = service.buildSessionParams(transaction);

        assertThat(params.getLineItems()).hasSize(1);
    }

    @Test
    void refusesAPaymentSessionForATransactionWithNothingToPay() {
        Transaction transaction = new Transaction();

        assertThatThrownBy(() -> service.buildSessionParams(transaction))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void returnsEmptyPaymentSessionWhenTransactionDoesNotExist() throws Exception {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThat(service.createPaymentSession("missing")).isEmpty();
    }

    private ProductItem productItem(String name, String description, Long price, int quantity) {
        Product product = new Product();
        product.setProductName(name);
        product.setDescription(description);
        product.setPrice(price);
        ProductItem item = new ProductItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private void authorizeCheckout(Transaction transaction) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(CHECKOUT_ACCESS_TOKEN.getBytes(StandardCharsets.UTF_8));
            transaction.setCheckoutAccessTokenDigest(
                Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
