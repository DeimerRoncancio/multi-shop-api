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
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentsServiceImpl implements PaymentService {
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
            .filter(transaction -> checkoutAccessToken.matches(transaction.getCheckoutAccessTokenDigest(), accessToken))
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
    public Optional<Transaction> updateProducts(String id, List<ProductItemDTO> products) {
        return repository.findById(id).map(transaction -> {
            replaceItems(transaction, products);
            return transaction;
        });
    }

    private void replaceItems(Transaction transaction, List<ProductItemDTO> products) {
        transaction.getProductItems().clear();
        products.forEach(item ->
            transaction.addItem(productRepository.findById(item.id()).orElse(null), item.quantity())
        );
        transaction.setTotalPrice(transaction.calculateTotalPrice());
    }

    @Override
    @Transactional
    public Optional<Transaction> deleteTransaction(String id) {
        return repository.findById(id).map(transaction -> {
           repository.delete(transaction);
           return transaction;
        });
    }
}
