package com.multi.shop.identity.stats;

import com.multi.shop.identity.users.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class AppController {
    private final UserService userService;
    private final CatalogStatsClient catalogStats;

    public AppController(UserService userService, CatalogStatsClient catalogStats) {
        this.userService = userService;
        this.catalogStats = catalogStats;
    }

    @GetMapping("/quantity")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> appStats() {
        Map<String, Long> catalog = catalogStats.stats();
        Map<String, Long> appStats = new HashMap<>();

        appStats.put("users", userService.usersSize());
        appStats.put("products", catalog.getOrDefault("products", 0L));
        appStats.put("categories", catalog.getOrDefault("categories", 0L));

        return appStats;
    }
}
