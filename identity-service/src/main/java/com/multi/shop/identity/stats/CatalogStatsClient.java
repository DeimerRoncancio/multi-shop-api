package com.multi.shop.identity.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Component
public class CatalogStatsClient {
    private static final Logger log = LoggerFactory.getLogger(CatalogStatsClient.class);
    private static final ParameterizedTypeReference<Map<String, Long>> STATS = new ParameterizedTypeReference<>() {};

    private final RestClient client;

    public CatalogStatsClient(RestClient.Builder builder, @Value("${catalog.service.url}") String baseUrl) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.client = builder.clone()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }

    public Map<String, Long> stats() {
        try {
            Map<String, Long> stats = client.get().uri("/internal/stats").retrieve().body(STATS);
            return stats == null ? Map.of() : stats;
        } catch (RestClientException exception) {
            log.warn("The catalog service could not read its stats: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The catalog service is not available");
        }
    }
}
