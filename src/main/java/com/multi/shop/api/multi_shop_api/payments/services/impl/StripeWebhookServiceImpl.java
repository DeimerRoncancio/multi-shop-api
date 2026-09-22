package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.enums.TransactionStatus;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.services.StripeWebhookService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class StripeWebhookServiceImpl implements StripeWebhookService {
    private static final Logger log = LoggerFactory.getLogger(StripeWebhookServiceImpl.class);

    private final PaymentsRepository repository;

    public StripeWebhookServiceImpl(PaymentsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void webhookEvent(String payload, String sigHeader, String webhookKey) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, sigHeader, webhookKey);

        switch (event.getType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                sessionFrom(event).ifPresent(session -> {
                    if ("paid".equals(session.getPaymentStatus())) approvePayment(session);
                });
            case "checkout.session.async_payment_failed", "checkout.session.expired" ->
                sessionFrom(event).ifPresent(this::rejectPayment);
            default -> log.debug("Ignoring Stripe event {}", event.getType());
        }
    }

    private Optional<Session> sessionFrom(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> object = deserializer.getObject();

        if (object.isEmpty()) {
            try {
                object = Optional.of(deserializer.deserializeUnsafe());
            } catch (EventDataObjectDeserializationException exception) {
                log.error("Could not read Stripe event {}: {}", event.getId(), exception.getMessage());
                return Optional.empty();
            }
        }

        return object.filter(Session.class::isInstance).map(Session.class::cast);
    }

    private void approvePayment(Session session) {
        transactionOf(session).ifPresent(transaction -> {
            if (transaction.getStatus() == TransactionStatus.APPROVED) return;

            long expectedAmount = transaction.payableAmountInCents();

            if (session.getAmountTotal() == null || session.getAmountTotal() != expectedAmount) {
                log.warn("Stripe session {} charged {} but transaction {} expects {}",
                    session.getId(), session.getAmountTotal(), transaction.getId(), expectedAmount);
                return;
            }

            transaction.setStatus(TransactionStatus.APPROVED);
            transaction.setTransactionDate(new Date());
        });
    }

    private void rejectPayment(Session session) {
        transactionOf(session).ifPresent(transaction -> {
            if (transaction.getStatus() == TransactionStatus.APPROVED) return;
            if (transaction.getStatus() == TransactionStatus.REJECTED) return;
            if (transaction.getStripeSessionId() != null && !transaction.getStripeSessionId().equals(session.getId())) return;

            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setTransactionDate(new Date());
        });
    }

    private Optional<Transaction> transactionOf(Session session) {
        String transactionId = session.getMetadata() != null && session.getMetadata().get("transactionId") != null
            ? session.getMetadata().get("transactionId")
            : session.getClientReferenceId();

        if (transactionId == null) {
            log.warn("Stripe session {} has no transaction", session.getId());
            return Optional.empty();
        }

        Optional<Transaction> transaction = repository.findById(transactionId);
        if (transaction.isEmpty()) log.warn("Stripe session {} points to missing transaction {}", session.getId(), transactionId);

        return transaction;
    }
}
