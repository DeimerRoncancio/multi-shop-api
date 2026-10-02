package com.multi.shop.api.multi_shop_api.transactions.services.impl;

import com.multi.shop.api.multi_shop_api.transactions.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.transactions.dtos.NewTransactionDTO;
import com.multi.shop.api.multi_shop_api.transactions.dtos.ProductItemDTO;
import com.multi.shop.api.multi_shop_api.transactions.dtos.TransactionAccessDTO;
import com.multi.shop.api.multi_shop_api.transactions.entities.Transaction;
import com.multi.shop.api.multi_shop_api.transactions.enums.TransactionStatus;
import com.multi.shop.api.multi_shop_api.transactions.mappers.CheckoutMapper;
import com.multi.shop.api.multi_shop_api.transactions.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.transactions.security.CheckoutAccessToken;
import com.multi.shop.api.multi_shop_api.transactions.services.PaymentService;
import com.multi.shop.api.multi_shop_api.catalog.api.CatalogApi;
import com.multi.shop.api.multi_shop_api.catalog.api.CatalogProduct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentsServiceImpl implements PaymentService {
    private final PaymentsRepository repository;
    private final CatalogApi catalogApi;
    private final CheckoutAccessToken checkoutAccessToken;
    private final CheckoutMapper checkoutMapper;

    public PaymentsServiceImpl(PaymentsRepository repository, CatalogApi catalogApi, CheckoutAccessToken checkoutAccessToken, CheckoutMapper checkoutMapper) {
        this.repository = repository;
        this.catalogApi = catalogApi;
        this.checkoutAccessToken = checkoutAccessToken;
        this.checkoutMapper = checkoutMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CheckoutSummaryDTO> getCheckoutSummary(String transactionId, String accessToken) {
        return repository.findById(transactionId)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken))
            .map(checkoutMapper::toCheckoutSummaryDTO);
    }

    @Override
    @Transactional
    public TransactionAccessDTO createTransaction(NewTransactionDTO dto) {
        Transaction transaction = new Transaction();
        String accessToken = checkoutAccessToken.generate();
        transaction.setCheckoutAccessTokenDigest(checkoutAccessToken.digest(accessToken));

        replaceItems(transaction, dto.productItems());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(Instant.now());
        repository.save(transaction);
        return new TransactionAccessDTO(transaction.getId(), accessToken);
    }

    @Override
    @Transactional
    public Optional<Transaction> updateProducts(String id, List<ProductItemDTO> products, String accessToken) {
        return repository.findById(id)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken))
            .map(transaction -> {
                if (transaction.getStatus() == TransactionStatus.APPROVED)
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

            CatalogProduct product = catalogApi.findProduct(item.id())
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
                if (transaction.getStatus() == TransactionStatus.APPROVED || transaction.getStatus() == TransactionStatus.PROCESSING)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction is paid or has a payment in course");

                repository.delete(transaction);
                return transaction;
            });
    }
}
