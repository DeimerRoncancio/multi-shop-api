package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.repositories.CustomersRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.dtos.*;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.entities.Guest;
import com.multi.shop.api.multi_shop_api.payments.services.PaymentService;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PaymentsServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentsServiceImpl.class);

    private final PaymentsRepository repository;
    private final ProductRepository productRepository;
    private final CustomersRepository customersRepository;
    private final UserRepository userRepository;

    @Value("${stripe.key.secret}")
    private String stripeKey;
    @Value("${stripe.success.url}")
    private String stripeSuccessUrl;
    @Value("${stripe.cancel.url}")
    private String stripeCancelUrl;

    public PaymentsServiceImpl (PaymentsRepository repository, ProductRepository productRepository, CustomersRepository customersRepository, UserRepository userRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.customersRepository = customersRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomer(String transactionId) {
        Optional<Transaction> transactionOp = repository.findById(transactionId);
        Customer customer = new Customer();

        if (transactionOp.isPresent()) customer = transactionOp.get().getCustomer();

        return Optional.ofNullable(customer);
    }

    @Override
    @Transactional
    public String createTransaction(NewTransactionDTO dto) {
        Transaction transaction = new Transaction();

        dto.productItems().forEach(item -> {
            ProductItem productItem = new ProductItem();
            productRepository.findById(item.id()).ifPresent(productItem::setProduct);
            productItem.setTransaction(transaction);
            productItem.setQuantity(item.quantity());
            transaction.getProductItems().add(productItem);
        });

        transaction.setTotalPrice(calculateTotalPrice(transaction));
        transaction.setStatus("Pending");
        repository.save(transaction);
        return transaction.getId();
    }

    private Long calculateTotalPrice(Transaction transaction) {
        return transaction.getProductItems().stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getPrice() != null)
                .mapToLong(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    @Override
    @Transactional
    public Optional<Transaction> updateProducts(String id, List<ProductItemDTO> products) {
        return repository.findById(id).map(transaction -> {
            transaction.getProductItems().clear();

            products.forEach(item -> {
                ProductItem productItem = new ProductItem();
                productRepository.findById(item.id()).ifPresent(productItem::setProduct);
                productItem.setTransaction(transaction);
                productItem.setQuantity(item.quantity());
                transaction.getProductItems().add(productItem);
            });

            transaction.setTotalPrice(calculateTotalPrice(transaction));
            return transaction;
        });
    }

    @Override
    @Transactional
    public void addTransactionDate(String transactionId, Date date){
        repository.findById(transactionId).map(transaction -> {
            transaction.setTransactionDate(date);
            return null;
        });
    }

    @Override
    @Transactional
    public Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId) {
        return repository.findById(transactionId).map(transaction -> {
            Customer customer = customersRepository
                .findByUser_EmailOrGuest_UserEmail(dto.userEmail(), dto.userEmail())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setCustomerAddress(dto.userAddress());

                    Optional<User> user = userRepository.findByEmail(dto.userEmail());
                    if (user.isPresent()) {
                        newCustomer.setUser(user.get());
                    } else {
                        Guest guest = new Guest();
                        guest.setUserNames(dto.userNames());
                        guest.setUserEmail(dto.userEmail());
                        guest.setUserPhone(dto.userPhone());
                        newCustomer.setGuest(guest);
                    }

                    return newCustomer;
                });

            transaction.setCustomer(customer);
            return repository.save(transaction);
        });
    }

    @Override
    @Transactional
    public void setStatus(String transactionId, String status) {
        repository.findById(transactionId).map(transaction -> {
            transaction.setStatus(status);
            return repository.save(transaction);
        });
    }

    @Override
    @Transactional
    public Optional<Transaction> deleteTransaction(String id) {
        return repository.findById(id).map(transaction -> {
           repository.delete(transaction);
           return transaction;
        });
    }

    public StripeResponseDTO createPaymentSession(StripeRequestDTO paymentSession, String transactionId) throws StripeException {
        List<StripeItemDTO> products = paymentSession.items();
        List<SessionCreateParams.LineItem> list = new ArrayList<>();

        for (StripeItemDTO product : products) {
            SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams
                    .LineItem.PriceData.ProductData.builder()
                    .setName(product.name())
                    .setDescription(product.description())
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
                .setSuccessUrl(stripeSuccessUrl)
                .setCancelUrl(stripeCancelUrl)
                .addAllLineItem(list)
                .build();

        Session session = Session.create(params);
        setStatus(transactionId, "PROCESSING");

        return new StripeResponseDTO(
            "SUCCESS",
            "Payment session creates",
            session.getId(),
            session.getUrl()
        );
    }

    public void webhookEvent(String payload, String sigHeader, String webhookKey) throws SignatureVerificationException {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookKey);
        } catch (SignatureVerificationException e) {
            log.warn("Signature verification failed: {}", String.valueOf(e));
            throw new SignatureVerificationException("Error", webhookKey);
        }
    }
}
