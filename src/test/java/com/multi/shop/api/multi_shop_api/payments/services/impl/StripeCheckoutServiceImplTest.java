package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.enums.TransactionStatus;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.security.CheckoutAccessToken;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeCheckoutServiceImplTest {
    private static final String CHECKOUT_ACCESS_TOKEN = "checkout-access-token";

    @Mock
    private PaymentsRepository repository;
    @Spy
    private CheckoutAccessToken checkoutAccessToken = new CheckoutAccessToken();

    @InjectMocks
    private StripeCheckoutServiceImpl service;

    @Test
    void remembersTheNewSessionAndExpiresThePreviousOpenOne() throws Exception {
        ReflectionTestUtils.setField(service, "stripeCurrency", "cop");
        Transaction transaction = payableTransaction();
        transaction.setStripeSessionId("cs_old");
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        Session oldSession = stripeSession("cs_old", "open");
        Session newSession = stripeSession("cs_new", "open");

        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            stripe.when(() -> Session.retrieve("cs_old")).thenReturn(oldSession);
            stripe.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(newSession);

            service.createPaymentSession("transaction-id", CHECKOUT_ACCESS_TOKEN);
        }

        verify(oldSession).expire();
        assertThat(transaction.getStripeSessionId()).isEqualTo("cs_new");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PROCESSING);
    }

    @Test
    void cancelsTheOpenSessionWithAValidAccessToken() throws Exception {
        Transaction transaction = payableTransaction();
        transaction.setStripeSessionId("cs_open");
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        Session session = stripeSession("cs_open", "open");

        boolean cancelled;
        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            stripe.when(() -> Session.retrieve("cs_open")).thenReturn(session);

            cancelled = service.cancelPaymentSession("transaction-id", CHECKOUT_ACCESS_TOKEN);
        }

        assertThat(cancelled).isTrue();
        verify(session).expire();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void doesNotExpireASessionThatIsAlreadyClosed() throws Exception {
        Transaction transaction = payableTransaction();
        transaction.setStripeSessionId("cs_paid");
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));
        Session session = stripeSession("cs_paid", "complete");

        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            stripe.when(() -> Session.retrieve("cs_paid")).thenReturn(session);

            assertThat(service.cancelPaymentSession("transaction-id", CHECKOUT_ACCESS_TOKEN)).isTrue();
        }

        verify(session, never()).expire();
    }

    @Test
    void refusesToCancelWithoutAValidAccessToken() throws Exception {
        Transaction transaction = payableTransaction();
        transaction.setStripeSessionId("cs_open");
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            assertThat(service.cancelPaymentSession("transaction-id", "wrong-token")).isFalse();
            assertThat(service.cancelPaymentSession("transaction-id", null)).isFalse();
            stripe.verifyNoInteractions();
        }
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

        assertThat(service.createPaymentSession("missing", CHECKOUT_ACCESS_TOKEN)).isEmpty();
    }

    @Test
    void refusesToCreateAPaymentSessionWithoutAValidAccessToken() throws Exception {
        Transaction transaction = payableTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            assertThat(service.createPaymentSession("transaction-id", "wrong-token")).isEmpty();
            assertThat(service.createPaymentSession("transaction-id", null)).isEmpty();
            stripe.verifyNoInteractions();
        }

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void refusesToChargeATransactionThatIsAlreadyPaid() {
        Transaction transaction = payableTransaction();
        transaction.setStatus(TransactionStatus.APPROVED);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            assertThatThrownBy(() -> service.createPaymentSession("transaction-id", CHECKOUT_ACCESS_TOKEN))
                .isInstanceOf(ResponseStatusException.class);
            stripe.verifyNoInteractions();
        }
    }

    private Transaction payableTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId("transaction-id");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCheckoutAccessTokenDigest(new CheckoutAccessToken().digest(CHECKOUT_ACCESS_TOKEN));
        transaction.getProductItems().add(productItem("Hamburguesa", "Clásica", 38000L, 1));
        return transaction;
    }

    private Session stripeSession(String id, String status) {
        Session session = mock(Session.class);
        lenient().when(session.getId()).thenReturn(id);
        lenient().when(session.getStatus()).thenReturn(status);
        return session;
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
}
