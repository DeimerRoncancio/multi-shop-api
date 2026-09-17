package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceImplTest {
    private static final String WEBHOOK_SECRET = "whsec_test";

    @Mock
    private PaymentsRepository repository;

    @InjectMocks
    private StripeWebhookServiceImpl service;

    @Test
    void approvesTheTransactionWhenStripeConfirmsThePayment() throws Exception {
        Transaction transaction = processingTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.completed", "paid", 7_600_000L);

        assertThat(transaction.getStatus()).isEqualTo("APPROVED");
        assertThat(transaction.getTransactionDate()).isNotNull();
    }

    @Test
    void approvingTheSamePaymentTwiceKeepsItApproved() throws Exception {
        Transaction transaction = processingTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.completed", "paid", 7_600_000L);
        Date approvedAt = transaction.getTransactionDate();
        sendWebhook("checkout.session.completed", "paid", 7_600_000L);

        assertThat(transaction.getStatus()).isEqualTo("APPROVED");
        assertThat(transaction.getTransactionDate()).isSameAs(approvedAt);
    }

    @Test
    void approvingAfterACancelledAttemptRefreshesTheDate() throws Exception {
        Transaction transaction = processingTransaction();
        Date cancelledAt = new Date(0);
        transaction.setTransactionDate(cancelledAt);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.completed", "paid", 7_600_000L);

        assertThat(transaction.getStatus()).isEqualTo("APPROVED");
        assertThat(transaction.getTransactionDate()).isAfter(cancelledAt);
    }

    @Test
    void cancellingAgainAfterARetryRefreshesTheDate() throws Exception {
        Transaction transaction = processingTransaction();
        Date firstCancelAt = new Date(0);
        transaction.setTransactionDate(firstCancelAt);
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.expired", "unpaid", 7_600_000L);

        assertThat(transaction.getStatus()).isEqualTo("REJECTED");
        assertThat(transaction.getTransactionDate()).isAfter(firstCancelAt);
    }

    @Test
    void aRepeatedExpirationDoesNotChangeTheRejectionDate() throws Exception {
        Transaction transaction = processingTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.expired", "unpaid", 7_600_000L);
        Date rejectedAt = transaction.getTransactionDate();
        sendWebhook("checkout.session.expired", "unpaid", 7_600_000L);

        assertThat(transaction.getTransactionDate()).isSameAs(rejectedAt);
    }

    @Test
    void waitsWhenTheCheckoutIsCompletedButNotPaidYet() throws Exception {
        Transaction transaction = processingTransaction();

        sendWebhook("checkout.session.completed", "unpaid", 7_600_000L);

        assertThat(transaction.getStatus()).isEqualTo("PROCESSING");
        verify(repository, never()).findById(any());
    }

    @Test
    void doesNotApproveWhenTheChargedAmountDiffersFromTheTransaction() throws Exception {
        Transaction transaction = processingTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.completed", "paid", 100L);

        assertThat(transaction.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    void rejectsTheTransactionWhenTheSessionExpires() throws Exception {
        Transaction transaction = processingTransaction();
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.expired", "unpaid", 7_600_000L);

        assertThat(transaction.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void ignoresTheExpirationOfAnOldSessionWhenTheCustomerRetriedThePayment() throws Exception {
        Transaction transaction = processingTransaction();
        transaction.setStripeSessionId("cs_retry");
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.expired", "unpaid", 7_600_000L);

        assertThat(transaction.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    void aLateFailureDoesNotUndoAnApprovedPayment() throws Exception {
        Transaction transaction = processingTransaction();
        transaction.setStatus("APPROVED");
        when(repository.findById("transaction-id")).thenReturn(Optional.of(transaction));

        sendWebhook("checkout.session.async_payment_failed", "unpaid", 7_600_000L);

        assertThat(transaction.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void refusesAWebhookWithAnInvalidSignature() {
        String payload = sessionEvent("checkout.session.completed", "paid", 7_600_000L);

        assertThatThrownBy(() -> service.webhookEvent(payload, sign(payload, "other-secret"), WEBHOOK_SECRET))
            .isInstanceOf(SignatureVerificationException.class);
        verify(repository, never()).findById(any());
    }

    private Transaction processingTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId("transaction-id");
        transaction.setStatus("PROCESSING");
        transaction.setStripeSessionId("cs_test");
        transaction.getProductItems().add(productItem("Hamburguesa", "Clásica", 38000L, 2));
        return transaction;
    }

    private void sendWebhook(String type, String paymentStatus, long amountTotal) throws Exception {
        String payload = sessionEvent(type, paymentStatus, amountTotal);
        service.webhookEvent(payload, sign(payload, WEBHOOK_SECRET), WEBHOOK_SECRET);
    }

    private String sessionEvent(String type, String paymentStatus, long amountTotal) {
        return """
            {
              "id": "evt_test",
              "object": "event",
              "api_version": "%s",
              "type": "%s",
              "data": {
                "object": {
                  "id": "cs_test",
                  "object": "checkout.session",
                  "client_reference_id": "transaction-id",
                  "metadata": {"transactionId": "transaction-id"},
                  "payment_status": "%s",
                  "amount_total": %d,
                  "currency": "cop"
                }
              }
            }
            """.formatted(Stripe.API_VERSION, type, paymentStatus, amountTotal);
    }

    private String sign(String payload, String secret) throws Exception {
        long timestamp = Webhook.Util.getTimeNow();
        String signature = Webhook.Util.computeHmacSha256(secret, timestamp + "." + payload);
        return "t=" + timestamp + ",v1=" + signature;
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
