package com.multi.shop.api.multi_shop_api.payments;

import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.payments.dtos.*;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.services.impl.PaymentsServiceImpl;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/app/payments")
@CrossOrigin(originPatterns = "*")
public class PaymentsController {
    private final PaymentsServiceImpl service;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public PaymentsController(PaymentsServiceImpl service) {
        this.service = service;
    }

    @GetMapping("/get-customer/{transactionId}")
    public ResponseEntity<Void> getCustomer(@PathVariable("transactionId") String transactionId) {
        service.getCustomer(transactionId).orElseThrow(() -> new NotFoundException("Customer not found"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/{transactionId}/{email}")
    public ResponseEntity<CustomerCheckoutDTO> getCheckoutCustomer(
        @PathVariable String transactionId,
        @PathVariable String email
    ) {
        CustomerCheckoutDTO customer = service.getCheckoutCustomer(transactionId, email)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

        return ResponseEntity.ok(customer);
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
    public ResponseEntity<TransactionAccessDTO> createTransaction(@RequestBody NewTransactionDTO dto) {
        return ResponseEntity.ok().body(service.createTransaction(dto));
    }

    @PutMapping("/update-products/{transactionId}")
    public ResponseEntity<Void> updateProducts(
        @PathVariable String transactionId,
        @RequestBody List<ProductItemDTO> products
    ) {
        service.updateProducts(transactionId, products)
            .orElseThrow(() -> new NotFoundException("Transaction not found"));

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/add-transaction-date/{transactionId}")
    public ResponseEntity<Void> addTransactionDate(@PathVariable("transactionId") String transactionId, @RequestBody Date date) {
        service.addTransactionDate(transactionId, date);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/add-user/{transactionId}")
    public ResponseEntity<String> addUser(@RequestBody UserTransactionDTO userDTO, @PathVariable String transactionId) {
        Optional<Transaction> transactionOp = service.addUserToTransaction(userDTO, transactionId);

        Transaction transaction = transactionOp.orElseThrow(() -> new NotFoundException("Transaction not found"));
        Customer customer = transaction.getCustomer();

        if (customer.isGuest())
            return ResponseEntity.status(HttpStatus.CREATED).body(customer.getGuest().getUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PutMapping("/set-status/{transactionId}/{status}")
    public ResponseEntity<Void> setStatus(@PathVariable("transactionId") String transactionId, @PathVariable("status") String status) {
        service.setStatus(transactionId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") String id) {
        Optional<Transaction> transactionOp = service.deleteTransaction(id);
        transactionOp.orElseThrow(() -> new NotFoundException("Transaction not found"));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-payment-session/{transactionId}")
    public ResponseEntity<StripeResponseDTO> createPaymentSession(@PathVariable String transactionId) throws StripeException {
        StripeResponseDTO session = service.createPaymentSession(transactionId)
            .orElseThrow(() -> new NotFoundException("Transaction not found"));

        return ResponseEntity.ok().body(session);
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
            service.webhookEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException exception) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        return ResponseEntity.ok().body("Success");
    }
}
