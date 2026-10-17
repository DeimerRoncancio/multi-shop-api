package com.multi.shop.transactions.services.impl;

import com.multi.shop.transactions.dtos.PurchaseDTO;
import com.multi.shop.transactions.enums.TransactionStatus;
import com.multi.shop.transactions.identity.IdentityApi;
import com.multi.shop.transactions.mappers.PurchaseMapper;
import com.multi.shop.transactions.repositories.PaymentsRepository;
import com.multi.shop.transactions.services.PurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class PurchaseServiceImpl implements PurchaseService {
    private final PaymentsRepository repository;
    private final IdentityApi identityApi;
    private final PurchaseMapper purchaseMapper;

    public PurchaseServiceImpl(PaymentsRepository repository, IdentityApi identityApi, PurchaseMapper purchaseMapper) {
        this.repository = repository;
        this.identityApi = identityApi;
        this.purchaseMapper = purchaseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseDTO> getUserPurchases(String userIdentity) {
        return identityApi.findByIdentity(userIdentity)
            .map(account -> repository.findByCustomer_UserEmailIgnoreCaseAndStatusNotOrderByCreatedAtDesc(account.email(), TransactionStatus.PENDING))
            .map(purchaseMapper::toPurchaseDTOs)
            .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseDTO> getGuestPurchases(String email) {
        if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo es obligatorio");

        String key = email.trim().toLowerCase(Locale.ROOT);
        return purchaseMapper.toPurchaseDTOs(
            repository.findByCustomer_Guest_UserEmailIgnoreCaseAndStatusNotOrderByCreatedAtDesc(key, TransactionStatus.PENDING)
        );
    }
}
