package com.multi.shop.transactions.catalog;

import com.multi.shop.transactions.catalog.CatalogApi;
import com.multi.shop.transactions.catalog.CatalogProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
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
    private static final ParameterizedTypeReference<List<CatalogProduct>> PRODUCT_LIST = new ParameterizedTypeReference<>() {};

    private final RestClient client;

    public CatalogApiHttpClient(RestClient.Builder builder, @Value("${catalog.service.url}") String baseUrl) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.client = builder.clone()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }

    @Override
    public Optional<CatalogProduct> findProduct(String id) {
        if (id == null) return Optional.empty();

        try {
            return Optional.ofNullable(client.get().uri("/internal/products/{id}", id).retrieve().body(CatalogProduct.class));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw unavailable("read the product " + id, exception);
        }
    }

    @Override
    public Map<String, CatalogProduct> findProducts(Collection<String> ids) {
        List<String> wanted = ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (wanted.isEmpty()) return Map.of();

        try {
            List<CatalogProduct> products = client.get()
                .uri(uri -> uri.path("/internal/products").queryParam("ids", wanted.toArray()).build())
                .retrieve()
                .body(PRODUCT_LIST);
            return products == null
                ? Map.of()
                : products.stream().collect(Collectors.toMap(CatalogProduct::id, Function.identity(), (first, second) -> first));
        } catch (RestClientException exception) {
            throw unavailable("read " + wanted.size() + " products", exception);
        }
    }

    private static ResponseStatusException unavailable(String action, RestClientException exception) {
        log.warn("The catalog service could not {}: {}", action, exception.getMessage());
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The catalog service is not available");
    }
}
