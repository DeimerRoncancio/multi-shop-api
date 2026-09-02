package com.multi.shop.api.multi_shop_api.payments;

import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.payments.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.StripeRequestDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.StripeResponseDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.services.impl.PaymentsServiceImpl;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    @PostMapping("/create-payment-session")
    public StripeResponseDTO createPaymentSession(@RequestBody @Valid StripeRequestDTO paymentSession)
    throws StripeException {
        return service.createPaymentSession(paymentSession);
    }

    @PostMapping("/create-transaction")
    public ResponseEntity<String> createTransaction(@RequestBody NewTransactionDTO dto) {
        return ResponseEntity.ok().body(service.createTransaction(dto));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") String id) {
        Optional<Transaction> transactionOp = service.deleteTransaction(id);
        transactionOp.orElseThrow(() -> new NotFoundException("Transaction not found"));

        return ResponseEntity.ok().build();
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
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader)
    throws SignatureVerificationException {
        service.webhookEvent(payload, sigHeader, webhookSecret);
        return ResponseEntity.ok().body("Success");
    }
}
