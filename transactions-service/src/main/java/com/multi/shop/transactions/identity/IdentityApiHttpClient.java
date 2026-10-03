package com.multi.shop.transactions.identity;

import com.multi.shop.transactions.identity.Account;
import com.multi.shop.transactions.identity.IdentityApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

@Service
public class IdentityApiHttpClient implements IdentityApi {
    private static final Logger log = LoggerFactory.getLogger(IdentityApiHttpClient.class);

    private final RestClient client;

    public IdentityApiHttpClient(RestClient.Builder builder, @Value("${identity.service.url}") String baseUrl) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.client = builder.clone()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }

    @Override
    public Optional<Account> findByIdentity(String identity) {
        if (identity == null || identity.isBlank()) return Optional.empty();
        return findAccount("identity", identity);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return findAccount("email", email);
    }

    private Optional<Account> findAccount(String field, String value) {
        try {
            return Optional.ofNullable(client.get()
                .uri(uri -> uri.path("/internal/accounts").queryParam(field, value).build())
                .retrieve()
                .body(Account.class));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw unavailable("read an account by " + field, exception);
        }
    }

    private static ResponseStatusException unavailable(String action, RestClientException exception) {
        log.warn("The identity service could not {}: {}", action, exception.getMessage());
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The identity service is not available");
    }
}
