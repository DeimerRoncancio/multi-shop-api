package com.multi.shop.api.multi_shop_api.payments;

import com.multi.shop.api.multi_shop_api.payments.dtos.StripeRequestDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.StripeResponseDTO;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/payments")
@CrossOrigin(originPatterns = "*")
public class PaymentsController {
    private final PaymentsService service;

    public PaymentsController(PaymentsService service) {
        this.service = service;
    }

    @PostMapping("/create-payment-session")
    public StripeResponseDTO createPaymentSession(@RequestBody @Valid StripeRequestDTO paymentSession)
    throws StripeException {
        return service.createPaymentSession(paymentSession);
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
    public String webhook(@RequestBody String payload, @RequestHeader("Stripe-signature") String sigHeader){
        System.out.println(sigHeader);
        return "stripeWebhook";
    }
}
