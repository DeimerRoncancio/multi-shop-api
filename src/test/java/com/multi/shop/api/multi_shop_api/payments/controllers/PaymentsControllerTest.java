package com.multi.shop.api.multi_shop_api.payments.controllers;

import com.multi.shop.api.multi_shop_api.common.controllers.ControllerAdvice;
import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutProductItemDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.services.CheckoutCustomerService;
import com.multi.shop.api.multi_shop_api.payments.services.PaymentService;
import com.multi.shop.api.multi_shop_api.payments.services.StripeCheckoutService;
import com.multi.shop.api.multi_shop_api.payments.services.StripeWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentsControllerTest {
    private PaymentService service;
    private CheckoutCustomerService customerService;
    private StripeCheckoutService stripeCheckoutService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(PaymentService.class);
        customerService = mock(CheckoutCustomerService.class);
        stripeCheckoutService = mock(StripeCheckoutService.class);
        mockMvc = MockMvcBuilders
            .standaloneSetup(new PaymentsController(
                service,
                customerService,
                stripeCheckoutService,
                mock(StripeWebhookService.class)
            ))
            .setControllerAdvice(new ControllerAdvice())
            .build();
    }

    @Test
    void returnsCheckoutSummary() throws Exception {
        CustomerAddressDTO address = new CustomerAddressDTO(
            "Casa", "Calle 1", "Bogota", "Cundinamarca", "Colombia", "3001234567"
        );
        CheckoutSummaryDTO summary = new CheckoutSummaryDTO(
            "transaction-id",
            "Pending",
            50000L,
            new CustomerSummaryDTO("Guest User", "guest@example.com", "3001234567"),
            List.of(address),
            address,
            List.of(new CheckoutProductItemDTO("product-id", "Cafe", 25000L, 2))
        );
        when(service.getCheckoutSummary("transaction-id", "valid-token"))
            .thenReturn(Optional.of(summary));

        mockMvc.perform(get("/app/payments/checkout/transaction-id")
                .header("X-Checkout-Access-Token", "valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").value("transaction-id"))
            .andExpect(jsonPath("$.status").value("Pending"))
            .andExpect(jsonPath("$.totalPrice").value(50000))
            .andExpect(jsonPath("$.customer.userEmail").value("guest@example.com"))
            .andExpect(jsonPath("$.addresses[0].addressName").value("Casa"))
            .andExpect(jsonPath("$.selectedAddress.address").value("Calle 1"))
            .andExpect(jsonPath("$.items[0].id").value("product-id"))
            .andExpect(jsonPath("$.items[0].productName").value("Cafe"))
            .andExpect(jsonPath("$.items[0].price").value(25000))
            .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void forbidsChangingATransactionWithoutAccessToken() throws Exception {
        when(service.updateProducts(any(), anyList(), isNull())).thenReturn(Optional.empty());
        when(service.deleteTransaction(any(), isNull())).thenReturn(Optional.empty());
        when(customerService.addUserToTransaction(any(), any(), isNull())).thenReturn(Optional.empty());
        when(stripeCheckoutService.createPaymentSession(any(), isNull())).thenReturn(Optional.empty());

        mockMvc.perform(put("/app/payments/update-products/transaction-id")
                .contentType("application/json").content("[]"))
            .andExpect(status().isForbidden());
        mockMvc.perform(put("/app/payments/add-user/transaction-id")
                .contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/app/payments/create-payment-session/transaction-id"))
            .andExpect(status().isForbidden());
        mockMvc.perform(delete("/app/payments/transaction-id"))
            .andExpect(status().isForbidden());
    }

    @Test
    void returnsForbiddenWhenCheckoutAccessTokenIsMissing() throws Exception {
        when(service.getCheckoutSummary("transaction-id", null)).thenReturn(Optional.empty());

        mockMvc.perform(get("/app/payments/checkout/transaction-id"))
            .andExpect(status().isForbidden());
    }

    @Test
    void returnsForbiddenWhenCheckoutAccessTokenIsInvalid() throws Exception {
        when(service.getCheckoutSummary("transaction-id", "invalid-token"))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/app/payments/checkout/transaction-id")
                .header("X-Checkout-Access-Token", "invalid-token"))
            .andExpect(status().isForbidden());
    }
}
