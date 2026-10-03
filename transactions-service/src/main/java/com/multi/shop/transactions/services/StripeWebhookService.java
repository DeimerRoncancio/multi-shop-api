package com.multi.shop.transactions.services;

import com.stripe.exception.SignatureVerificationException;

public interface StripeWebhookService {
    void webhookEvent(String payload, String sigHeader, String webhookKey) throws SignatureVerificationException;
}
