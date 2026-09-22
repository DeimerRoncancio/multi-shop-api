package com.multi.shop.api.multi_shop_api.payments.controllers;

import com.multi.shop.api.multi_shop_api.payments.dtos.*;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.services.CheckoutCustomerService;
import com.multi.shop.api.multi_shop_api.payments.services.PaymentService;
import com.multi.shop.api.multi_shop_api.payments.services.StripeCheckoutService;
import com.multi.shop.api.multi_shop_api.payments.services.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/app/payments")
public class PaymentsController {
    private final PaymentService service;
    private final CheckoutCustomerService customerService;
    private final StripeCheckoutService stripeCheckoutService;
    private final StripeWebhookService stripeWebhookService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public PaymentsController(
        PaymentService service,
        CheckoutCustomerService customerService,
        StripeCheckoutService stripeCheckoutService,
        StripeWebhookService stripeWebhookService
    ) {
        this.service = service;
        this.customerService = customerService;
        this.stripeCheckoutService = stripeCheckoutService;
        this.stripeWebhookService = stripeWebhookService;
    }

    @GetMapping("/saved-addresses")
    public List<CustomerAddressDTO> getSavedAddresses(Principal principal) {
        return customerService.getSavedAddresses(principal.getName());
    }

    @GetMapping("/checkout/{transactionId}")
    public ResponseEntity<CheckoutSummaryDTO> getCheckoutSummary(
        @PathVariable String transactionId,
        @RequestHeader(value = "X-Checkout-Access-Token", required = false) String checkoutAccessToken
    ) {
        return service.getCheckoutSummary(transactionId, checkoutAccessToken)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @PostMapping("/create-transaction")
    public ResponseEntity<TransactionAccessDTO> createTransaction(@Valid @RequestBody NewTransactionDTO dto) {
        return ResponseEntity.ok().body(service.createTransaction(dto));
    }

    @PutMapping("/update-products/{transactionId}")
    public ResponseEntity<Void> updateProducts(
        @PathVariable String transactionId,
        @RequestBody List<ProductItemDTO> products,
        @RequestHeader(value = "X-Checkout-Access-Token", required = false) String checkoutAccessToken
    ) {
        return service.updateProducts(transactionId, products, checkoutAccessToken).isPresent()
            ? ResponseEntity.status(HttpStatus.CREATED).build()
            : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/add-user/{transactionId}")
    public ResponseEntity<String> addUser(
        @Valid @RequestBody UserTransactionDTO userDTO,
        @PathVariable String transactionId,
        @RequestHeader(value = "X-Checkout-Access-Token", required = false) String checkoutAccessToken,
        Principal principal
    ) {
        String userIdentity = principal == null ? null : principal.getName();
        Optional<Transaction> transactionOp = customerService.addUserToTransaction(userDTO, transactionId, checkoutAccessToken, userIdentity);
        if (transactionOp.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Customer customer = transactionOp.get().getCustomer();
        if (customer.isGuest())
            return ResponseEntity.status(HttpStatus.CREATED).body(customer.getGuest().getUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
        @PathVariable("id") String id,
        @RequestHeader(value = "X-Checkout-Access-Token", required = false) String checkoutAccessToken
    ) {
        return service.deleteTransaction(id, checkoutAccessToken).isPresent()
            ? ResponseEntity.ok().build()
            : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/create-payment-session/{transactionId}")
    public ResponseEntity<StripeResponseDTO> createPaymentSession(
        @PathVariable String transactionId,
        @RequestHeader(value = "X-Checkout-Access-Token", required = false) String checkoutAccessToken
    ) throws StripeException {
        return stripeCheckoutService.createPaymentSession(transactionId, checkoutAccessToken)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @PostMapping("/cancel-payment-session/{transactionId}")
    public ResponseEntity<Void> cancelPaymentSession(
        @PathVariable String transactionId,
        @RequestHeader(value = "X-Checkout-Access-Token", required = false) String checkoutAccessToken
    ) throws StripeException {
        return stripeCheckoutService.cancelPaymentSession(transactionId, checkoutAccessToken)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/success")
    public Map<String, String> success() {
        Map<String, String> response = new HashMap<>();

        response.put("Ok", "true");
        response.put("message", "Payment successful");

        return response;
    }

    @GetMapping("/cancel")
    public Map<String, String> cancel() {
        Map<String, String> response = new HashMap<>();

        response.put("Ok", "false");
        response.put("message", "Payment cancelled");

        return response;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            stripeWebhookService.webhookEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException exception) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        return ResponseEntity.ok().body("Success");
    }
}
