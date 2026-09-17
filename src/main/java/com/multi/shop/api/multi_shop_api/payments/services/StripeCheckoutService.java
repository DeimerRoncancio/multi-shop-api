package com.multi.shop.api.multi_shop_api.payments.services;

import com.multi.shop.api.multi_shop_api.payments.dtos.StripeResponseDTO;
import com.stripe.exception.StripeException;

import java.util.Optional;

public interface StripeCheckoutService {
    Optional<StripeResponseDTO> createPaymentSession(String transactionId) throws StripeException;
    boolean cancelPaymentSession(String transactionId, String checkoutAccessToken) throws StripeException;
}
