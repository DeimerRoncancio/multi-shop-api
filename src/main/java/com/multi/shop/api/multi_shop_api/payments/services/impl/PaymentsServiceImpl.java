package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.entities.*;
import com.multi.shop.api.multi_shop_api.payments.mappers.CheckoutMapper;
import com.multi.shop.api.multi_shop_api.payments.mappers.CustomerMapper;
import com.multi.shop.api.multi_shop_api.payments.mappers.TransactionMapper;
import com.multi.shop.api.multi_shop_api.payments.repositories.AddressRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.CustomersRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.dtos.*;
import com.multi.shop.api.multi_shop_api.payments.services.PaymentService;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

@Service
public class PaymentsServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentsServiceImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final PaymentsRepository repository;
    private final ProductRepository productRepository;
    private final CustomersRepository customersRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Value("${stripe.key.secret}")
    private String stripeKey;
    @Value("${stripe.success.url}")
    private String stripeSuccessUrl;
    @Value("${stripe.cancel.url}")
    private String stripeCancelUrl;
    @Value("${stripe.currency:cop}")
    private String stripeCurrency;

    public PaymentsServiceImpl (PaymentsRepository repository, ProductRepository productRepository, CustomersRepository customersRepository, AddressRepository addressRepository, UserRepository userRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.customersRepository = customersRepository;
        this.addressRepository = addressRepository;
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
    @Transactional(readOnly = true)
    public Optional<CustomerCheckoutDTO> getCheckoutCustomer(String transactionId, String email) {
        return repository.findById(transactionId).flatMap(transaction -> {
            Customer customer = transaction.getCustomer();

            if (customer != null) {
                return customerHasEmail(customer, email)
                    ? Optional.of(CustomerMapper.MAPPER.toCustomerCheckoutDTO(customer, transaction))
                    : Optional.empty();
            }

            return customersRepository
                .findByUser_EmailOrGuest_UserEmail(email, email)
                .map(customerMatch -> CustomerMapper.MAPPER.toCustomerCheckoutDTO(customerMatch, transaction));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CheckoutSummaryDTO> getCheckoutSummary(String transactionId, String checkoutAccessToken) {
        return repository.findById(transactionId)
            .filter(transaction -> hasCheckoutAccess(transaction, checkoutAccessToken))
            .map(CheckoutMapper.MAPPER::toCheckoutSummaryDTO);
    }

    private boolean customerHasEmail(Customer customer, String email) {
        return customer.isGuest()
            ? email.equals(customer.getGuest().getUserEmail())
            : customer.getUser() != null && email.equals(customer.getUser().getEmail());
    }

    @Override
    @Transactional
    public TransactionAccessDTO createTransaction(NewTransactionDTO dto) {
        Transaction transaction = new Transaction();
        byte[] accessTokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(accessTokenBytes);
        String checkoutAccessToken = Base64.getUrlEncoder().withoutPadding().encodeToString(accessTokenBytes);
        transaction.setCheckoutAccessTokenDigest(digestCheckoutAccessToken(checkoutAccessToken));

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
        return new TransactionAccessDTO(transaction.getId(), checkoutAccessToken);
    }

    private boolean hasCheckoutAccess(Transaction transaction, String checkoutAccessToken) {
        if (transaction.getCheckoutAccessTokenDigest() == null || checkoutAccessToken == null) return false;

        return MessageDigest.isEqual(
            transaction.getCheckoutAccessTokenDigest().getBytes(StandardCharsets.UTF_8),
            digestCheckoutAccessToken(checkoutAccessToken).getBytes(StandardCharsets.UTF_8)
        );
    }

    private String digestCheckoutAccessToken(String checkoutAccessToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(checkoutAccessToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
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
            Customer customer = getCustomer(dto);
            Address address = getAddress(dto.userAddress(), customer);

            address.setCustomer(customer);
            if (!customer.getAddress().contains(address))
                customer.getAddress().add(address);

            if (customer.getId() == null) customersRepository.save(customer);
            transaction.setCustomer(customer);
            ShippingAddress shippingAddress = TransactionMapper.MAPPER.toShippingAddress(dto.userAddress());
            transaction.setShippingAddress(shippingAddress);
            return repository.save(transaction);
        });
    }

    public Customer getCustomer(UserTransactionDTO dto) {
        return customersRepository
            .findByUser_EmailOrGuest_UserEmail(dto.userEmail(), dto.userEmail())
            .map(customer -> {
                if (customer.isGuest()) {
                    customer.getGuest().setUserNames(dto.userNames());
                    customer.getGuest().setUserEmail(dto.userEmail());
                    customer.getGuest().setUserPhone(dto.userPhone());
                }

                return customer;
            }).orElseGet(() -> {
                Customer newCustomer = new Customer();

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
    }

    public Address getAddress(CustomerAddressDTO dto, Customer customer) {
        Address address = customer.getId() == null
            ? new Address()
            : addressRepository.findByCustomerAndAddressName(customer, dto.addressName())
                .orElseGet(Address::new);

        return TransactionMapper.MAPPER.updateAddress(dto, address);
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

    @Transactional
    public Optional<StripeResponseDTO> createPaymentSession(String transactionId) throws StripeException {
        Optional<Transaction> transactionOp = repository.findById(transactionId);
        if (transactionOp.isEmpty()) return Optional.empty();

        Transaction transaction = transactionOp.get();
        Session session = Session.create(buildSessionParams(transaction));
        transaction.setStatus("PROCESSING");

        return Optional.of(new StripeResponseDTO(
            "SUCCESS",
            "Payment session created",
            session.getId(),
            session.getUrl()
        ));
    }

    SessionCreateParams buildSessionParams(Transaction transaction) {
        List<SessionCreateParams.LineItem> lineItems = payableItems(transaction)
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

    private Stream<ProductItem> payableItems(Transaction transaction) {
        return transaction.getProductItems().stream()
            .filter(item -> item.getProduct() != null && item.getProduct().getPrice() != null)
            .filter(item -> item.getQuantity() > 0);
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

    @Transactional
    public void webhookEvent(String payload, String sigHeader, String webhookKey) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, sigHeader, webhookKey);

        switch (event.getType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                sessionFrom(event).ifPresent(session -> {
                    if ("paid".equals(session.getPaymentStatus())) approvePayment(session);
                });
            case "checkout.session.async_payment_failed", "checkout.session.expired" ->
                sessionFrom(event).ifPresent(this::rejectPayment);
            default -> log.debug("Ignoring Stripe event {}", event.getType());
        }
    }

    private Optional<Session> sessionFrom(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> object = deserializer.getObject();

        if (object.isEmpty()) {
            try {
                object = Optional.of(deserializer.deserializeUnsafe());
            } catch (EventDataObjectDeserializationException exception) {
                log.error("Could not read Stripe event {}: {}", event.getId(), exception.getMessage());
                return Optional.empty();
            }
        }

        return object.filter(Session.class::isInstance).map(Session.class::cast);
    }

    private void approvePayment(Session session) {
        transactionOf(session).ifPresent(transaction -> {
            long expectedAmount = payableItems(transaction)
                .mapToLong(item -> item.getProduct().getPrice() * 100 * item.getQuantity())
                .sum();

            if (session.getAmountTotal() == null || session.getAmountTotal() != expectedAmount) {
                log.warn("Stripe session {} charged {} but transaction {} expects {}",
                    session.getId(), session.getAmountTotal(), transaction.getId(), expectedAmount);
                return;
            }

            transaction.setStatus(STATUS_APPROVED);
            if (transaction.getTransactionDate() == null) transaction.setTransactionDate(new Date());
        });
    }

    private void rejectPayment(Session session) {
        transactionOf(session).ifPresent(transaction -> {
            if (STATUS_APPROVED.equals(transaction.getStatus())) return;

            transaction.setStatus(STATUS_REJECTED);
            if (transaction.getTransactionDate() == null) transaction.setTransactionDate(new Date());
        });
    }

    private Optional<Transaction> transactionOf(Session session) {
        String transactionId = session.getMetadata() != null && session.getMetadata().get("transactionId") != null
            ? session.getMetadata().get("transactionId")
            : session.getClientReferenceId();

        if (transactionId == null) {
            log.warn("Stripe session {} has no transaction", session.getId());
            return Optional.empty();
        }

        Optional<Transaction> transaction = repository.findById(transactionId);
        if (transaction.isEmpty()) log.warn("Stripe session {} points to missing transaction {}", session.getId(), transactionId);

        return transaction;
    }
}
