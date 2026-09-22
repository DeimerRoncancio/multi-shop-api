package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.TransactionAccessDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.mappers.CheckoutMapper;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.security.CheckoutAccessToken;
import com.multi.shop.api.multi_shop_api.payments.services.PaymentService;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentsServiceImpl implements PaymentService {
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_PROCESSING = "PROCESSING";

    private final PaymentsRepository repository;
    private final ProductRepository productRepository;
    private final CheckoutAccessToken checkoutAccessToken;

    public PaymentsServiceImpl(PaymentsRepository repository, ProductRepository productRepository, CheckoutAccessToken checkoutAccessToken) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.checkoutAccessToken = checkoutAccessToken;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CheckoutSummaryDTO> getCheckoutSummary(String transactionId, String accessToken) {
        return repository.findById(transactionId)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken))
            .map(CheckoutMapper.MAPPER::toCheckoutSummaryDTO);
    }

    @Override
    @Transactional
    public TransactionAccessDTO createTransaction(NewTransactionDTO dto) {
        Transaction transaction = new Transaction();
        String accessToken = checkoutAccessToken.generate();
        transaction.setCheckoutAccessTokenDigest(checkoutAccessToken.digest(accessToken));

        replaceItems(transaction, dto.productItems());
        transaction.setStatus("Pending");
        repository.save(transaction);
        return new TransactionAccessDTO(transaction.getId(), accessToken);
    }

    @Override
    @Transactional
    public Optional<Transaction> updateProducts(String id, List<ProductItemDTO> products, String accessToken) {
        return repository.findById(id)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken))
            .map(transaction -> {
                if (STATUS_APPROVED.equals(transaction.getStatus()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction is already paid");

                replaceItems(transaction, products);
                return transaction;
            });
    }

    private void replaceItems(Transaction transaction, List<ProductItemDTO> products) {
        if (products == null || products.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction needs at least one product");

        transaction.getProductItems().clear();
        products.forEach(item -> {
            if (item.quantity() < 1)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");

            Product product = productRepository.findById(item.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + item.id()));

            transaction.addItem(product, item.quantity());
        });
        transaction.setTotalPrice(transaction.calculateTotalPrice());
    }

    @Override
    @Transactional
    public Optional<Transaction> deleteTransaction(String id, String accessToken) {
        return repository.findById(id)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken))
            .map(transaction -> {
                if (STATUS_APPROVED.equals(transaction.getStatus()) || STATUS_PROCESSING.equals(transaction.getStatus()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction is paid or has a payment in course");

                repository.delete(transaction);
                return transaction;
            });
    }
}
