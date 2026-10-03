package com.multi.shop.identity.stats;

import com.multi.shop.identity.users.services.UserService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class AppController {
    private static final Logger log = LoggerFactory.getLogger(AppController.class);

    private final UserService userService;
    private final CatalogStatsClient catalogStats;

    public AppController(UserService userService, CatalogStatsClient catalogStats) {
        this.userService = userService;
        this.catalogStats = catalogStats;
    }

    @GetMapping("/quantity")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> appStats() {
        Map<String, Long> catalog = catalogStats();
        Map<String, Long> appStats = new HashMap<>();

        appStats.put("users", userService.usersSize());
        appStats.put("products", catalog.getOrDefault("products", 0L));
        appStats.put("categories", catalog.getOrDefault("categories", 0L));

        return appStats;
    }

    private Map<String, Long> catalogStats() {
        try {
            Map<String, Long> stats = catalogStats.stats();
            return stats == null ? Map.of() : stats;
        } catch (FeignException exception) {
            log.warn("The catalog service could not read its stats: {}", exception.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The catalog service is not available");
        }
    }
}
