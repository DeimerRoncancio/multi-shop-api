package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.TransactionAccessDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Guest;
import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import com.multi.shop.api.multi_shop_api.payments.entities.ShippingAddress;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.mappers.TransactionMapper;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.security.CheckoutAccessToken;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceImplTest {
    private static final String CHECKOUT_ACCESS_TOKEN = "checkout-access-token";

    @Mock
    private PaymentsRepository repository;
    @Mock
    private ProductRepository productRepository;
    @Spy
    private CheckoutAccessToken checkoutAccessToken = new CheckoutAccessToken();

    @InjectMocks
    private PaymentsServiceImpl service;

    @Test
    void createsTransactionWithAnOpaqueAccessTokenAndPersistsOnlyItsDigest() {
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId("transaction-id");
            return transaction;
        });

        when(productRepository.findById("product-id")).thenReturn(Optional.of(new Product()));

        TransactionAccessDTO access = service.createTransaction(
            new NewTransactionDTO(List.of(new ProductItemDTO("product-id", 1)), "pending")
        );

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
    void replacesTheProductsWithAValidAccessToken() {
        Product product = new Product();
        product.setPrice(25000L);
        Transaction transaction = new Transaction();
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(productRepository.findById("product-id")).thenReturn(Optional.of(product));

        Optional<Transaction> result = service.updateProducts(
            "transaction-id",
            List.of(new ProductItemDTO("product-id", 3)),
            CHECKOUT_ACCESS_TOKEN
        );

        assertThat(result).contains(transaction);
        assertThat(transaction.getProductItems()).singleElement()
            .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(3));
        assertThat(transaction.getTotalPrice()).isEqualTo(75000L);
    }

    @Test
    void refusesProductsThatDoNotExist() {
        Transaction transaction = new Transaction();
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        when(productRepository.findById("no-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProducts(
            "transaction-id",
            List.of(new ProductItemDTO("no-existe", 1)),
            CHECKOUT_ACCESS_TOKEN
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Product not found");

        assertThat(transaction.getProductItems()).isEmpty();
    }

    @Test
    void refusesQuantitiesBelowOne() {
        Transaction transaction = new Transaction();
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        for (int quantity : new int[] {0, -5}) {
            assertThatThrownBy(() -> service.updateProducts(
                "transaction-id",
                List.of(new ProductItemDTO("product-id", quantity)),
                CHECKOUT_ACCESS_TOKEN
            )).isInstanceOf(ResponseStatusException.class);
        }

        assertThat(transaction.getProductItems()).isEmpty();
        verifyNoInteractions(productRepository);
    }

    @Test
    void refusesAnEmptyProductList() {
        Transaction transaction = new Transaction();
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> service.updateProducts("transaction-id", List.of(), CHECKOUT_ACCESS_TOKEN))
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void refusesToChangeProductsWithoutAValidAccessToken() {
        Transaction transaction = new Transaction();
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        List<ProductItemDTO> products = List.of(new ProductItemDTO("product-id", 3));

        assertThat(service.updateProducts("transaction-id", products, "wrong-token")).isEmpty();
        assertThat(service.updateProducts("transaction-id", products, null)).isEmpty();
        assertThat(transaction.getProductItems()).isEmpty();
        verifyNoInteractions(productRepository);
    }

    @Test
    void refusesToChangeTheProductsOfAPaidTransaction() {
        Transaction transaction = new Transaction();
        transaction.setStatus("APPROVED");
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> service.updateProducts(
            "transaction-id",
            List.of(new ProductItemDTO("product-id", 3)),
            CHECKOUT_ACCESS_TOKEN
        )).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deletesAPendingTransactionWithAValidAccessToken() {
        Transaction transaction = new Transaction();
        transaction.setStatus("Pending");
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThat(service.deleteTransaction("transaction-id", CHECKOUT_ACCESS_TOKEN)).contains(transaction);
        verify(repository).delete(transaction);
    }

    @Test
    void refusesToDeleteWithoutAValidAccessToken() {
        Transaction transaction = new Transaction();
        authorizeCheckout(transaction);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        assertThat(service.deleteTransaction("transaction-id", "wrong-token")).isEmpty();
        assertThat(service.deleteTransaction("transaction-id", null)).isEmpty();
        verify(repository, never()).delete(any());
    }

    @Test
    void refusesToDeleteATransactionThatIsBeingPaidOrIsPaid() {
        for (String status : List.of("PROCESSING", "APPROVED")) {
            Transaction transaction = new Transaction();
            transaction.setStatus(status);
            authorizeCheckout(transaction);
            when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

            assertThatThrownBy(() -> service.deleteTransaction("transaction-id", CHECKOUT_ACCESS_TOKEN))
                .isInstanceOf(ResponseStatusException.class);
        }

        verify(repository, never()).delete(any());
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
