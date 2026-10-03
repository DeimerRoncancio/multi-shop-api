package com.multi.shop.identity.stats;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "catalog", url = "${catalog.service.url}")
public interface CatalogStatsClient {
    @GetMapping("/internal/stats")
    Map<String, Long> stats();
}
