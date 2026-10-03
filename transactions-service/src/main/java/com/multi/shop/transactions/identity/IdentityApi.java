package com.multi.shop.transactions.identity;

import java.util.Optional;

public interface IdentityApi {
    Optional<Account> findByIdentity(String identity);

    Optional<Account> findByEmail(String email);
}
