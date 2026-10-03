package com.multi.shop.transactions.catalog;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CatalogApiHttpClient implements CatalogApi {
    private static final Logger log = LoggerFactory.getLogger(CatalogApiHttpClient.class);

    private final CatalogClient client;

    public CatalogApiHttpClient(CatalogClient client) {
        this.client = client;
    }

    @Override
    public Optional<CatalogProduct> findProduct(String id) {
        if (id == null) return Optional.empty();

        try {
            return Optional.ofNullable(client.findProduct(id));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw unavailable("read the product " + id, exception);
        }
    }

    @Override
    public Map<String, CatalogProduct> findProducts(Collection<String> ids) {
        List<String> wanted = ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (wanted.isEmpty()) return Map.of();

        try {
            List<CatalogProduct> products = client.findProducts(wanted);
            return products == null
                ? Map.of()
                : products.stream().collect(Collectors.toMap(CatalogProduct::id, Function.identity(), (first, second) -> first));
        } catch (FeignException exception) {
            throw unavailable("read " + wanted.size() + " products", exception);
        }
    }

    private static ResponseStatusException unavailable(String action, FeignException exception) {
        log.warn("The catalog service could not {}: {}", action, exception.getMessage());
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The catalog service is not available");
    }
}
