package com.multi.shop.api.multi_shop_api.identity.users.services.impl;

import com.multi.shop.api.multi_shop_api.identity.api.Account;
import com.multi.shop.api.multi_shop_api.identity.api.IdentityApi;
import com.multi.shop.api.multi_shop_api.identity.users.entities.User;
import com.multi.shop.api.multi_shop_api.identity.users.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IdentityApiImpl implements IdentityApi {
    private final UserRepository repository;

    public IdentityApiImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByIdentity(String identity) {
        return repository.findByIdentity(identity).map(IdentityApiImpl::toAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return repository.findByEmail(email).map(IdentityApiImpl::toAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return repository.count();
    }

    private static Account toAccount(User user) {
        return new Account(user.getName(), user.getEmail(), user.getPhoneNumber());
    }
}
