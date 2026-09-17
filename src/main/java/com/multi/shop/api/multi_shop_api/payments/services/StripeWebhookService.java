package com.multi.shop.api.multi_shop_api.payments.services;

import com.stripe.exception.SignatureVerificationException;

public interface StripeWebhookService {
    void webhookEvent(String payload, String sigHeader, String webhookKey) throws SignatureVerificationException;
}
