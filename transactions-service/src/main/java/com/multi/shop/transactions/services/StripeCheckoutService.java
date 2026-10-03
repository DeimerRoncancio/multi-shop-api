package com.multi.shop.transactions.services;

import com.multi.shop.transactions.dtos.StripeResponseDTO;
import com.stripe.exception.StripeException;

import java.util.Optional;

public interface StripeCheckoutService {
    Optional<StripeResponseDTO> createPaymentSession(String transactionId, String checkoutAccessToken) throws StripeException;
    boolean cancelPaymentSession(String transactionId, String checkoutAccessToken) throws StripeException;
}
