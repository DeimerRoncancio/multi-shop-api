package com.multi.shop.api.multi_shop_api.identity.api;

import java.util.Optional;

public interface IdentityApi {
    Optional<Account> findByIdentity(String identity);

    Optional<Account> findByEmail(String email);

    long countUsers();
}
