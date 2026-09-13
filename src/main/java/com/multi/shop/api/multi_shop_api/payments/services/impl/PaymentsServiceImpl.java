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
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Value("${stripe.key.secret}")
    private String stripeKey;
    @Value("${stripe.success.url}")
    private String stripeSuccessUrl;
    @Value("${stripe.cancel.url}")
    private String stripeCancelUrl;

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
                    ? Optional.of(toCustomerCheckoutDTO(customer, transaction))
                    : Optional.empty();
            }

            return customersRepository
                .findByUser_EmailOrGuest_UserEmail(email, email)
                .map(customerMatch -> toCustomerCheckoutDTO(customerMatch, transaction));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CheckoutSummaryDTO> getCheckoutSummary(String transactionId) {
        return repository.findById(transactionId).map(CheckoutMapper.MAPPER::toCheckoutSummaryDTO);
    }

    private CustomerCheckoutDTO toCustomerCheckoutDTO(Customer customer, Transaction transaction) {
        CustomerCheckoutDTO customerDTO = CustomerMapper.MAPPER.toCustomerCheckoutDTO(customer);
        CustomerAddressDTO selectedAddress = TransactionMapper.MAPPER
            .toCustomerAddressDTO(transaction.getShippingAddress());

        return new CustomerCheckoutDTO(
            customerDTO.userNames(),
            customerDTO.userEmail(),
            customerDTO.userPhone(),
            customerDTO.addresses(),
            selectedAddress
        );
    }

    private boolean customerHasEmail(Customer customer, String email) {
        return customer.isGuest()
            ? email.equals(customer.getGuest().getUserEmail())
            : customer.getUser() != null && email.equals(customer.getUser().getEmail());
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
