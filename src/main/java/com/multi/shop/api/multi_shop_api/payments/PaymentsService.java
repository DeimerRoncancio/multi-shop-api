package com.multi.shop.api.multi_shop_api.payments;

import com.multi.shop.api.multi_shop_api.payments.dtos.StripeItemDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.StripeRequestDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.StripeResponseDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PaymentsService {
    @Value("${stripe.key.secret}")
    private String stripeKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
    }

    public StripeResponseDTO createPaymentSession(StripeRequestDTO paymentSession) throws StripeException {
        List<StripeItemDTO> products = paymentSession.items();
        List<SessionCreateParams.LineItem> list = new ArrayList<>();

        for (StripeItemDTO product : products) {
            SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams
                    .LineItem.PriceData.ProductData.builder()
                    .setName(product.name())
                    .build();

            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams
                    .LineItem.PriceData.builder()
                    .setCurrency(paymentSession.currency())
                    .setProductData(productData)
                    .setUnitAmount(product.price())
                    .build();

            SessionCreateParams.LineItem lineItem = SessionCreateParams
                    .LineItem.builder()
                    .setPriceData(priceData)
                    .setQuantity(product.quantity())
                    .build();

            list.add(lineItem);
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5000/app/payments/success")
                .setCancelUrl("http://localhost:5000/app/payments/cancel")
                .addAllLineItem(list)
                .build();

        Session session = Session.create(params);

        return new StripeResponseDTO(
            "SUCCESS",
            "Payment session creates",
            session.getId(),
            session.getUrl()
        );
    }
}
