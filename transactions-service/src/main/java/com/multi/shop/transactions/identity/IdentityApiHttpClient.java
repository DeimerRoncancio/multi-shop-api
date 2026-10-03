package com.multi.shop.transactions.identity;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdentityApiHttpClient implements IdentityApi {
    private static final Logger log = LoggerFactory.getLogger(IdentityApiHttpClient.class);

    private final IdentityClient client;

    public IdentityApiHttpClient(IdentityClient client) {
        this.client = client;
    }

    @Override
    public Optional<Account> findByIdentity(String identity) {
        if (identity == null || identity.isBlank()) return Optional.empty();
        return findAccount("identity", () -> client.findByIdentity(identity));
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return findAccount("email", () -> client.findByEmail(email));
    }

    private Optional<Account> findAccount(String field, Supplier<Account> call) {
        try {
            return Optional.ofNullable(call.get());
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            log.warn("The identity service could not read an account by {}: {}", field, exception.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The identity service is not available");
        }
    }
}
