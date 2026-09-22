package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.StripeResponseDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.enums.TransactionStatus;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.security.CheckoutAccessToken;
import com.multi.shop.api.multi_shop_api.payments.services.StripeCheckoutService;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class StripeCheckoutServiceImpl implements StripeCheckoutService {
    private final PaymentsRepository repository;
    private final CheckoutAccessToken checkoutAccessToken;

    @Value("${stripe.key.secret}")
    private String stripeKey;
    @Value("${stripe.success.url}")
    private String stripeSuccessUrl;
    @Value("${stripe.cancel.url}")
    private String stripeCancelUrl;
    @Value("${stripe.currency:cop}")
    private String stripeCurrency;

    public StripeCheckoutServiceImpl(PaymentsRepository repository, CheckoutAccessToken checkoutAccessToken) {
        this.repository = repository;
        this.checkoutAccessToken = checkoutAccessToken;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
    }

    @Override
    @Transactional
    public Optional<StripeResponseDTO> createPaymentSession(String transactionId, String accessToken) throws StripeException {
        Optional<Transaction> transactionOp = repository.findById(transactionId)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken));
        if (transactionOp.isEmpty()) return Optional.empty();

        Transaction transaction = transactionOp.get();
        if (transaction.getStatus() == TransactionStatus.APPROVED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction is already paid");

        SessionCreateParams params = buildSessionParams(transaction);
        expireOpenSession(transaction);

        Session session = Session.create(params);
        transaction.setStripeSessionId(session.getId());
        transaction.setStatus(TransactionStatus.PROCESSING);

        return Optional.of(new StripeResponseDTO(
            "SUCCESS",
            "Payment session created",
            session.getId(),
            session.getUrl()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean cancelPaymentSession(String transactionId, String accessToken) throws StripeException {
        Optional<Transaction> transactionOp = repository.findById(transactionId)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken));

        if (transactionOp.isEmpty()) return false;

        expireOpenSession(transactionOp.get());
        return true;
    }

    private void expireOpenSession(Transaction transaction) throws StripeException {
        if (transaction.getStripeSessionId() == null) return;

        Session session = Session.retrieve(transaction.getStripeSessionId());
        if ("open".equals(session.getStatus())) session.expire();
    }

    SessionCreateParams buildSessionParams(Transaction transaction) {
        List<SessionCreateParams.LineItem> lineItems = transaction.payableItems()
            .map(this::toLineItem)
            .toList();

        if (lineItems.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction has no payable items");

        return SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(stripeSuccessUrl)
            .setCancelUrl(stripeCancelUrl)
            .setClientReferenceId(transaction.getId())
            .putMetadata("transactionId", transaction.getId())
            .addAllLineItem(lineItems)
            .build();
    }

    private SessionCreateParams.LineItem toLineItem(ProductItem item) {
        Product product = item.getProduct();
        String description = product.getDescription() == null || product.getDescription().isBlank()
            ? product.getProductName()
            : product.getDescription().trim();

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams
            .LineItem.PriceData.ProductData.builder()
            .setName(product.getProductName())
            .setDescription(description)
            .build();

        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams
            .LineItem.PriceData.builder()
            .setCurrency(stripeCurrency)
            .setProductData(productData)
            .setUnitAmount(product.getPrice() * 100)
            .build();

        return SessionCreateParams.LineItem.builder()
            .setPriceData(priceData)
            .setQuantity((long) item.getQuantity())
            .build();
    }
}
